# import main Flask class and request object
import uuid
from datetime import datetime, date, time

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
    test_medicine_id: MedicineInfo("Vitamin B", "portion", Regularity.ONCE_A_WEEK, date(2022, 3, 8),
                                   date(2022, 3, 8), time(16))
}
user_to_medicine_ids: Dict[str, List[str]] = {
    test_user_id: [test_medicine_id]
}

users_list: Dict[str, UserInfo] = {test_user_id: UserInfo('test_user', 'test_user', '123456')}
users_to_dependents: Dict[str, List[str]] = {}
users_to_observers: Dict[str, List[str]] = {}
users_to_incoming_request: Dict[str, List[str]] = {}
users_to_outgoing_request: Dict[str, List[str]] = {}

# { date : {user: { medicine_id: TakeStatus } } }
date_to_medicine_status: Dict[date, Dict[str, Dict[str, TakeStatus]]] = {
    date(2022, 3, 8): {test_user_id: {test_medicine_id: TakeStatus.TAKEN}},
    date(2022, 3, 10): {test_user_id: {test_medicine_id: TakeStatus.NOT_TAKEN}},
    date(2022, 3, 11): {test_user_id: {test_medicine_id: TakeStatus.UNKNOWN}}
}


def from_json_to_medicine_info(json_request):
    return MedicineInfo(
        medicine_name=json_request['medicine_name'],
        portion=json_request['portion'],
        regularity=Regularity[json_request['regularity']],
        start_date=datetime.strptime(json_request['start_date'], '%Y-%m-%d').date(),
        end_date=datetime.strptime(json_request['end_date'], '%Y-%m-%d').date(),
        time=datetime.strptime(json_request['time'], '%H:%M').time()
    )


def from_medicine_info_to_json(medicine_info: MedicineInfo):
    return {
        'medicine_name': medicine_info.medicine_name,
        'portion': medicine_info.portion,
        'regularity': medicine_info.regularity,
        'start_date': medicine_info.start_date.strftime('%Y-%m-%d'),
        'end_date': medicine_info.end_date.strftime('%Y-%m-%d'),
        'time': medicine_info.time.strftime('%H:%M')
    }


def from_medicine_id_to_medicine_id_and_info_json(medicine_id):
    mi_json = from_medicine_info_to_json(medicine_id_to_medicine_info[medicine_id])
    mi_json['medicine_id'] = medicine_id
    return mi_json


@app.route('/add_medicine', methods=['POST'])
def add_medicine():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
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
        for take_date in medicine_info.regularity.take_dates_generator(medicine_info.start_date, medicine_info.end_date):
            if take_date not in date_to_medicine_status:
                date_to_medicine_status[take_date] = {}
            if user_id not in date_to_medicine_status[take_date]:
                date_to_medicine_status[take_date][user_id] = {}
            date_to_medicine_status[take_date][user_id][medicine_id] = TakeStatus.UNKNOWN
        return medicine_id, 200
    else:
        return f'Content-Type {content_type} not supported!', 404


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
        medicine_info = medicine_id_to_medicine_info[medicine_id]
        for take_date in medicine_info.regularity.take_dates_generator(medicine_info.start_date, medicine_info.end_date):
            if take_date not in date_to_medicine_status:
                raise KeyError()
            if user_id not in date_to_medicine_status[take_date]:
                raise KeyError()
            date_to_medicine_status[take_date][user_id].pop(medicine_id)
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
    # user_id = test_user_id
    take_date = request.args.get('date')
    if take_date is None:
        return 'Date must be provided', 400
    take_date = datetime.strptime(take_date, '%Y-%m-%d').date()
    if user_id not in users_list:
        return f'User with id {user_id} not found', 404
    return flask.jsonify(
        [{'medicine': from_medicine_id_to_medicine_id_and_info_json(medicine_id),
          'take_status': take_status}
         for medicine_id, take_status in date_to_medicine_status.get(take_date, {}).get(user_id, {}).items()]
    ), 200


@app.route('/medicines', methods=['GET'])
def get_medicines():
    user_id = request.args.get('user_id')
    if user_id is None:
        return 'User id must be provided', 400
    if user_id not in users_list:
        return f'User with id {user_id} not found', 404
    return flask.jsonify(
        [from_medicine_id_to_medicine_id_and_info_json(medicine_id)
         for medicine_id in user_to_medicine_ids[user_id]]
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


# Add dependent -> Send
@app.route('/dependent/send', methods=['POST'])
def dependent_send_request():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        dependent_id = json_request['dependent_id']
        if dependent_id not in users_list:
            return f'Could not find user {dependent_id}', 404
        users_to_outgoing_request[user_id].append(dependent_id)
        users_to_incoming_request[dependent_id].append(user_id)
        return f'Request to {user_id} was sent', 200
    else:
        return 'Content-Type not supported!', 404


# Incoming requests -> accept
@app.route('/incoming/accept', methods=['POST'])
def accept_observer_request():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        observer_id = json_request['observer_id']
        if observer_id not in users_list:
            return f'Could not find user {observer_id}', 404
        if observer_id not in users_to_incoming_request:
            return f'User {observer_id} declined his request', 200
        users_to_incoming_request[user_id].remove(observer_id)
        users_to_observers[user_id].append(observer_id)
        users_to_outgoing_request[observer_id].remove(user_id)
        users_to_dependents[observer_id].append(user_id)
        return f'User {user_id} added to observers', 200
    else:
        return 'Content-Type not supported!', 404


# Incoming requests -> decline
@app.route('/incoming/decline', methods=['POST'])
def decline_observer_request():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        observer_id = json_request['observer_id']
        if observer_id not in users_list:
            return f'Could not find user {observer_id}', 404
        if observer_id not in users_to_incoming_request[user_id]:
            return f'User {observer_id} already declined his request', 200
        users_to_incoming_request[user_id].remove(observer_id)
        users_to_outgoing_request[observer_id].remove(user_id)
        return f'Request from user {user_id} declined', 200
    else:
        return 'Content-Type not supported!', 404


# Outgoing requests -> withdraw
@app.route('/outgoing/withdraw', methods=['POST'])
def withdraw_outgoing_request():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        dependent_id = json_request['dependent_id']
        if dependent_id not in users_list:
            return f'Could not find user {dependent_id}', 404
        if dependent_id not in users_to_outgoing_request[user_id]:
            return f'User {dependent_id} already processed your request', 200
        users_to_incoming_request[dependent_id].remove(user_id)
        users_to_outgoing_request[user_id].remove(dependent_id)
        return f'Request to user {dependent_id} canceled', 200
    else:
        return 'Content-Type not supported!', 404


# Dependents -> remove
@app.route('/dependent/remove', methods=['POST'])
def dependent_remove():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        dependent_id = json_request['dependent_id']
        if dependent_id not in users_list:
            return f'Could not find user {dependent_id}', 404
        if dependent_id not in users_to_dependents[user_id]:
            return f'User {dependent_id} already removed', 200
        users_to_dependents[user_id].remove(dependent_id)
        users_to_observers[dependent_id].remove(user_id)
        return f'User {dependent_id} successfully removed', 200
    else:
        return 'Content-Type not supported!', 404


# Observers -> remove
@app.route('/observer/remove', methods=['POST'])
def observer_remove():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        observer_id = json_request['observer_id']
        if observer_id not in users_list:
            return f'Could not find user {observer_id}', 404
        if observer_id not in users_to_observers[user_id]:
            return f'User {observer_id} already removed', 200
        users_to_observers[user_id].remove(observer_id)
        users_to_dependents[observer_id].remove(user_id)
        return f'User {observer_id} successfully removed', 200
    else:
        return 'Content-Type not supported!', 404


if __name__ == '__main__':
    # run app in debug mode on port 5000
    app.run(debug=True, port=5000)
