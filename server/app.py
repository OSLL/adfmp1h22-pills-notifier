# import main Flask class and request object
import uuid
from datetime import datetime

import flask
import json
from flask import Flask, request
from typing import Dict, List
from models.medicine_info import Regularity, MedicineInfo
from models.user_info import UserInfo
from models.take_status import TakeStatus

# create the Flask app
app = Flask(__name__)

test_medicine_id = str(uuid.uuid4())
test_user_id = str(uuid.uuid4())

medicine_id_to_medicine_info: Dict[str, MedicineInfo] = {
    test_medicine_id: MedicineInfo("Vitamin B", "portion", "instructions", Regularity.ONCE_A_WEEK, datetime(2022, 1, 1),
                                   datetime.strptime('16:00', '%H:%M'))
}
user_to_medicine_ids: Dict[str, List[str]] = {
    test_user_id: [test_medicine_id]
}

users_list: Dict[str, UserInfo] = {test_user_id: UserInfo('test_user', 'test_user', '123456')}
users_to_dependents: Dict[str, List[str]] = {}

# { date : {user: { medicine_id: TakeStatus } } }
date_to_medicine_status: Dict[datetime, Dict[str, Dict[str, TakeStatus]]] = {
    datetime(2022, 3, 8): {test_user_id: {test_medicine_id: TakeStatus.TAKEN}}}


def from_json_to_medicine_info(json_request):
    medicine_name = json_request['medicine_name']
    instructions = json_request['instructions']
    portion = json_request['portion']
    regularity = Regularity[json_request['regularity']]
    format_date = '%Y-%m-%d'
    start_date = datetime.strptime(json_request['date'], format_date)
    format_time = '%H:%M'
    time = datetime.strptime(json_request['time'], format_time)
    return MedicineInfo(medicine_name=medicine_name, instructions=instructions, regularity=regularity,
                        start_date=start_date, time=time, portion=portion)


def from_medicine_info_to_json(medicine_info: MedicineInfo):
    return {
        'medicine_name': medicine_info.medicine_name,
        'portion': medicine_info.portion,
        'instructions': medicine_info.instructions,
        'regularity': medicine_info.regularity,
        'date': medicine_info.start_date.strftime('%Y-%m-%d'),
        'time': medicine_info.time.strftime('%H:%M')
    }


# TODO: specify end date
@app.route('/user/add_medicine', methods=['POST'])
def add_medicine():
    content_type = request.headers.get('Content-Type')
    if content_type == 'application/json':
        request_json = request.json
        if 'user_id' not in request_json:
            return 'User id must be provided', 400
        user_id = request_json['user_id']
        if user_id not in users_list:
            return f'User with id {user_id} not found', 404
        medicine_id = str(uuid.uuid4())
        medicine_info = from_json_to_medicine_info(request_json)
        medicine_id_to_medicine_info[medicine_id] = medicine_info
        if user_id in user_to_medicine_ids:
            user_to_medicine_ids[user_id].append(medicine_id)
        else:
            user_to_medicine_ids[user_id] = [medicine_id]
        return medicine_id, 200
    else:
        return 'Content-Type not supported!', 404


@app.route('/user/get_medicines', methods=['GET'])
def get_medicines():
    content_type = request.headers.get('Content-Type')
    if content_type == 'application/json':
        request_json = request.json
        if 'user_id' not in request_json:
            return 'User id must be provided', 400
        user_id = request_json['user_id']
        if user_id not in users_list:
            return f'User with id {user_id} not found', 404
        medicine_ids = user_to_medicine_ids.get(user_id, [])
        medicine = [medicine for medicine_id, medicine in medicine_id_to_medicine_info if medicine_id in medicine_ids]
        return flask.jsonify(
            [from_medicine_info_to_json(medicine_info) for medicine_info in medicine]
        ), 200
    else:
        return 'Content-Type not supported!', 404


@app.route('/user/delete_medicine', methods=['DELETE'])
def delete_medicine():
    content_type = request.headers.get('Content-Type')
    if content_type == 'application/json':
        request_json = request.json
        if 'user_id' not in request_json:
            return 'User id must be provided', 400
        user_id = request_json['user_id']
        medicine_id = request_json['medicine_id']
        if medicine_id not in user_to_medicine_ids.get(user_id, []):
            return f"Medicine with id {medicine_id} wasn't found"
        user_to_medicine_ids[user_id].remove(medicine_id)
        medicine_id_to_medicine_info.pop(medicine_id)
        return 'OK', 200
    else:
        return 'Content-Type not supported!', 404


@app.route('/schedule', methods=['GET'])
def get_schedule():
    user_id = request.args.get('user_id')
    if user_id is None:
        return 'User id must be provided', 400
    # will go away
    user_id = test_user_id
    date = request.args.get('date')
    if date is None:
        return 'Date must be provided', 400
    date = datetime.strptime(date, '%Y-%m-%d')
    if user_id not in users_list:
        return f'User with id {user_id} not found', 404
    return flask.jsonify(
        [{'medicine': from_medicine_info_to_json(medicine_id_to_medicine_info[medicine_id]),
          'take_status': take_status}
         for medicine_id, take_status in date_to_medicine_status.get(date, {}).get(user_id, {}).items()]
    ), 200


@app.route('/user/login', methods=['POST'])
def login():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        username = json_request['username']
        password = json_request['password']
        for user_id, user in users_list.items():
            if user.username == username and user.password == password:
                result = {'userId': user_id, 'fullname': user.fullname, 'username': username}
                return json.dumps(result), 200
        return 'Incorrect login or password', 404
    else:
        return 'Content-Type not supported!', 404


@app.route('/user/register', methods=['POST'])
def register():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        full_name = json_request['full_name']
        username = json_request['username']
        password = json_request['password']
        for user_id, user in users_list.items():
            if user.username == username:
                return 'Username already taken', 404
        user_id = str(uuid.uuid4())
        users_list[user_id] = UserInfo(full_name, username, password)
        result = {'userId': user_id, 'fullname': full_name, 'username': username}
        return json.dumps(result), 200
    else:
        return 'Content-Type not supported!', 404


@app.route('/user/update', methods=['POST'])
def update():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        full_name = json_request['full_name']
        username = json_request['username']
        for user_id_, user in users_list.items():
            if user.username == username and user_id_ != user_id:
                return 'Username already taken', 404
        if user_id not in users_list:
            return f'No user with id {user_id}', 404
        user = users_list[user_id]
        user.username = username
        user.fullname = full_name
        users_list[user_id] = user
        return 'OK', 200
    else:
        return 'Content-Type not supported!', 404


if __name__ == '__main__':
    # run app in debug mode on port 5000
    app.run(debug=True, port=5000)
