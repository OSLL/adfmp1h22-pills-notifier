# import main Flask class and request object
import uuid
from datetime import datetime

import flask
from flask import Flask, request
from typing import Dict, List
from models.medicine_info import Regularity, MedicineInfo
from models.user_info import UserInfo

# create the Flask app
app = Flask(__name__)

user_to_medicines: Dict[str, List[MedicineInfo]] = {}
users_list: Dict[str, UserInfo] = {'test_user_id': UserInfo('test_user', 'test_user', '123456')}


def from_json_to_medicine_info(json):
    medicine_name = json['medicine_name']
    instructions = json['instructions']
    portion = json['portion']
    regularity = Regularity[json['regularity']]
    format_date = '%Y-%m-%d'
    start_date = datetime.strptime(json['date'], format_date)
    format_time = '%H:%M'
    time = datetime.strptime(json['time'], format_time)
    return MedicineInfo(medicine_name=medicine_name, instructions=instructions, regularity=regularity,
                        start_date=start_date, time=time, portion=portion)


def from_medicine_info_to_json(medicine_info: MedicineInfo):
    return {
        'medicine_name': medicine_info.medicine_name,
        'portion': medicine_info.portion,
        'instructions': medicine_info.instructions,
        'regularity': str(medicine_info.regularity),
        'date': medicine_info.start_date.strftime('%Y-%m-%d'),
        'time': medicine_info.time.strftime('%H:%M')
    }


@app.route('/user/add_medicine', methods=['POST'])
def add_medicine():
    content_type = request.headers.get('Content-Type')
    if content_type == 'application/json':
        json = request.json
        user_id = json['user_id']
        medicine_info = from_json_to_medicine_info(json)
        if user_id in user_to_medicines:
            user_to_medicines[user_id].append(medicine_info)
        else:
            user_to_medicines[user_id] = [medicine_info]
        return 'OK'
    else:
        return 'Content-Type not supported!'


@app.route('/user/get_medicines', methods=['GET'])
def get_medicines():
    content_type = request.headers.get('Content-Type')
    if content_type == 'application/json':
        json = request.json
        user_id = json['user_id']
        return flask.jsonify(
            [from_medicine_info_to_json(medicine_info) for medicine_info in user_to_medicines.get(user_id, [])]
        )
    else:
        return 'Content-Type not supported!'


@app.route('/user/delete_medicine', methods=['DELETE'])
def delete_medicine():
    content_type = request.headers.get('Content-Type')
    if content_type == 'application/json':
        json = request.json
        user_id = json['user_id']
        medicine_info = from_json_to_medicine_info(json)
        if medicine_info not in user_to_medicines.get(user_id, []):
            return "Medicine wasn't found"
        user_to_medicines[user_id].remove(medicine_info)
        return 'OK'
    else:
        return 'Content-Type not supported!'


@app.route('/user/login', methods=['POST'])
def login():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json = request.json
        username = json['username']
        password = json['password']
        for user_id, user in users_list.items():
            if user.username == username and user.password == password:
                return user_id, 200
        return 'Incorrect login or password', 404
    else:
        return 'Content-Type not supported!', 404


@app.route('/user/register', methods=['POST'])
def register():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json = request.json
        full_name = json['full_name']
        username = json['username']
        password = json['password']
        for user_id, user in users_list.items():
            if user.username == username:
                return 'Username already taken', 404
        user_id = str(uuid.uuid4())
        users_list[user_id] = UserInfo(full_name, username, password)
        return user_id, 200
    else:
        return 'Content-Type not supported!', 404


if __name__ == '__main__':
    # run app in debug mode on port 5000
    app.run(debug=True, port=5000)
