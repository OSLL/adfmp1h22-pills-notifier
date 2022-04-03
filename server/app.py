# import main Flask class and request object
import uuid
from datetime import datetime, date, time

import flask
import json
from flask import Flask, request
from typing import Dict, List
from models.medicine_info import Regularity, MedicineInfo
from models.profile import Profile
from models.user_info import UserInfo
from models.take_status import TakeStatus
from models.notification import Notification

# create the Flask app
app = Flask(__name__)

test_medicine_id = str(uuid.uuid4())

test_user_id = str(uuid.uuid4())
snd_user_id = str(uuid.uuid4())
test_observer_id = str(uuid.uuid4())
sherlock_user_id = str(uuid.uuid4())
watson_user_id = str(uuid.uuid4())


medicine_id_to_medicine_info: Dict[str, MedicineInfo] = {
    test_medicine_id: MedicineInfo("Vitamin B", "portion", Regularity.DAILY, date(2022, 1, 1),
                                   date(2022, 4, 1), time(16))
}
user_to_medicine_ids: Dict[str, List[str]] = {
    test_user_id: [test_medicine_id]
}

users_list: Dict[str, UserInfo] = {test_user_id: UserInfo('test_user', 'test_user', '123456'),
                                   snd_user_id: UserInfo('snd_user', 'snd_user', '123456'),
                                   test_observer_id: UserInfo('test_observer', 'test_observer', '123456'),
                                   sherlock_user_id: UserInfo('Sherlock Holmes', 'sherlock_holmes', '123456'),
                                   watson_user_id: UserInfo('John Watson', 'john_watson', '123456')}
username_to_uuid: Dict[str, str] = {'test_user': test_user_id,
                                    'snd_user': snd_user_id,
                                    'test_observer': test_observer_id,
                                    'sherlock_holmes': sherlock_user_id,
                                    'john_watson': watson_user_id}

user_to_notifications: Dict[str, List[Notification]] = {
    test_user_id: [Notification('Kimberly White: Vitamin A not taken', '2022-04-01 15:00')]
}

users_to_dependents: Dict[str, List[str]] = {test_user_id: [snd_user_id, test_observer_id]}
users_to_observers: Dict[str, List[str]] = {snd_user_id: [test_user_id], test_observer_id: [test_user_id]}
users_to_incoming_request: Dict[str, List[str]] = {test_user_id: [sherlock_user_id], watson_user_id: [test_user_id]}
users_to_outgoing_request: Dict[str, List[str]] = {sherlock_user_id: [test_user_id], test_user_id: [watson_user_id]}

# { date : {user: { medicine_id: TakeStatus } } }
date_to_medicine_status: Dict[date, Dict[str, Dict[str, TakeStatus]]] = {}
test_medicine_info = medicine_id_to_medicine_info[test_medicine_id]
for take_date in Regularity.DAILY.take_dates_generator(test_medicine_info.start_date, test_medicine_info.end_date):
    date_to_medicine_status[take_date] = {test_user_id: {test_medicine_id: TakeStatus.UNKNOWN}}


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


def user_info_to_profile(user_info: UserInfo):
    return Profile(name=user_info.fullname, nickname=user_info.username)


def profiles_to_json(profile: Profile):
    return {
        'name': profile.name,
        'nickname': profile.nickname,
    }


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
        for take_date in medicine_info.regularity.take_dates_generator(medicine_info.start_date,
                                                                       medicine_info.end_date):
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
    if content_type.startswith('application/json'):
        request_json = request.json
        if 'user_id' not in request_json:
            return 'User id must be provided', 400
        user_id = request_json['user_id']
        medicine_id = request_json['medicine_id']
        if medicine_id not in user_to_medicine_ids.get(user_id, []):
            return f"Medicine with id {medicine_id} wasn't found"
        medicine_info = medicine_id_to_medicine_info[medicine_id]
        for take_date in medicine_info.regularity.take_dates_generator(medicine_info.start_date,
                                                                       medicine_info.end_date):
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
    take_date_datetime = datetime.strptime(take_date, '%Y-%m-%d').date()
    if user_id not in users_list:
        return f'User with id {user_id} not found', 404
    return flask.jsonify(
        [{'medicine': from_medicine_id_to_medicine_id_and_info_json(medicine_id),
          'take_status': take_status,
          'date': take_date
          }
         for medicine_id, take_status in date_to_medicine_status.get(take_date_datetime, {}).get(user_id, {}).items()]
    ), 200


@app.route('/notifications', methods=['GET'])
def get_notifications():
    user_id = request.args.get('user_id')
    if user_id is None:
        return 'User id must be provided', 400
    if user_id not in users_list:
        return f'User with id {user_id} not found', 404
    return flask.jsonify(
        [{'message': notification.message,
          'date': notification.date}
         for notification in user_to_notifications.get(user_id, [])]
    ), 200


@app.route('/medicine/status', methods=['POST'])
def add_status():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        if 'user_id' not in json_request:
            return 'User id must be provided', 400
        if 'date' not in json_request:
            return 'Date must be provided', 400
        if 'medicine_id' not in json_request:
            return 'Medicine id must be provided', 400
        if 'medicine_status' not in json_request:
            return 'Status id must be provided', 400
        user_id = json_request['user_id']
        if user_id not in users_list:
            return f'User with id {user_id} not found', 404
        medicine_date = datetime.strptime(json_request['date'], '%Y-%m-%d').date()
        medicine_id = json_request['medicine_id']
        medicine_status = TakeStatus[json_request['medicine_status']]
        observers = users_to_observers.get(user_id, [])
        medicine_name = medicine_id_to_medicine_info[medicine_id].medicine_name
        username = users_list.get(user_id).username
        notification_message = username + ": " + medicine_name + " " + medicine_status.name.lower()
        notification_date = json_request['date'] + " " + medicine_id_to_medicine_info[medicine_id].time.strftime('%H:%M')
        for observer_id in observers:
            user_to_notifications.get(observer_id, []).append(Notification(notification_message, notification_date))
        date_to_medicine_status[medicine_date][user_id][medicine_id] = medicine_status
        return 'OK', 200
    else:
        return 'Content-Type not supported!', 404


@app.route('/medicines', methods=['GET'])
def get_medicines():
    user_id = request.args.get('user_id')
    if user_id is None:
        return 'User id must be provided', 400
    if user_id not in users_list:
        return f'User with id {user_id} not found', 404
    return flask.jsonify(
        [from_medicine_id_to_medicine_id_and_info_json(medicine_id)
         for medicine_id in user_to_medicine_ids.get(user_id, [])]
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
        username_to_uuid[username] = user_id
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
        username_to_uuid.pop(username)
        username_to_uuid[username] = user_id
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
        dependent_username = json_request['dependent_username']
        if dependent_username not in username_to_uuid:
            return f'Could not find user {dependent_username}', 404
        dependent_id = username_to_uuid[dependent_username]
        if dependent_id not in users_list:
            return f'Could not find user {dependent_username}', 404
        if user_id == dependent_id:
            return f'Could not send request to yourself', 404
        if user_id not in users_to_outgoing_request:
            users_to_outgoing_request[user_id] = []
        users_to_outgoing_request[user_id].append(dependent_id)

        if dependent_id not in users_to_incoming_request:
            users_to_incoming_request[dependent_id] = []
        users_to_incoming_request[dependent_id].append(user_id)
        return f'Request to {dependent_username} was sent', 200
    else:
        return 'Content-Type not supported!', 404


# Incoming requests -> accept
@app.route('/incoming/accept', methods=['POST'])
def accept_observer_request():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        observer_username = json_request['username']
        if observer_username not in username_to_uuid:
            return f'Could not find user {observer_username}', 404
        observer_id = username_to_uuid[observer_username]
        if observer_id not in users_list:
            return f'Could not find user {observer_username}', 404
        if observer_id not in users_to_incoming_request[user_id]:
            return f'User {observer_username} declined his request', 200
        check_user_id_exists(observer_id)
        check_user_id_exists(user_id)
        users_to_incoming_request[user_id].remove(observer_id)
        users_to_observers[user_id].append(observer_id)
        users_to_outgoing_request[observer_id].remove(user_id)
        users_to_dependents[observer_id].append(user_id)
        return f'User {observer_username} added to observers', 200
    else:
        return 'Content-Type not supported!', 404


# Incoming requests -> decline
@app.route('/incoming/decline', methods=['POST'])
def decline_observer_request():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        observer_username = json_request['username']
        if observer_username not in username_to_uuid:
            return f'Could not find user {observer_username}', 404
        observer_id = username_to_uuid[observer_username]
        if observer_id not in users_list:
            return f'Could not find user {observer_username}', 404
        if observer_id not in users_to_incoming_request[user_id]:
            return f'User {observer_username} already declined his request', 200
        users_to_incoming_request[user_id].remove(observer_id)
        users_to_outgoing_request[observer_id].remove(user_id)
        return f'Request from user {observer_username} declined', 200
    else:
        return 'Content-Type not supported!', 404


# Outgoing requests -> withdraw
@app.route('/outgoing/withdraw', methods=['POST'])
def withdraw_outgoing_request():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        dependent_username = json_request['username']
        if dependent_username not in username_to_uuid:
            return f'Could not find user {dependent_username}', 404
        dependent_id = username_to_uuid[dependent_username]
        if dependent_id not in users_list:
            return f'Could not find user {dependent_username}', 404
        if dependent_id not in users_to_outgoing_request[user_id]:
            return f'User {dependent_username} already processed your request', 200
        users_to_incoming_request[dependent_id].remove(user_id)
        users_to_outgoing_request[user_id].remove(dependent_id)
        return f'Request to user {dependent_username} canceled', 200
    else:
        return 'Content-Type not supported!', 404


# Dependents -> remove
@app.route('/dependent/remove', methods=['POST'])
def dependent_remove():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        dependent_username = json_request['username']
        if dependent_username not in username_to_uuid:
            return f'Could not find user {dependent_username}', 404
        dependent_id = username_to_uuid[dependent_username]
        if dependent_id not in users_list:
            return f'Could not find user {dependent_username}', 404
        if dependent_id not in users_to_dependents[user_id]:
            return f'User {dependent_username} already removed', 200
        users_to_dependents[user_id].remove(dependent_id)
        users_to_observers[dependent_id].remove(user_id)
        return f'User {dependent_username} successfully removed', 200
    else:
        return 'Content-Type not supported!', 404


# Observers -> remove
@app.route('/observer/remove', methods=['POST'])
def observer_remove():
    content_type = request.headers.get('Content-Type')
    if content_type.startswith('application/json'):
        json_request = request.json
        user_id = json_request['user_id']
        observer_username = json_request['username']
        if observer_username not in username_to_uuid:
            return f'Could not find user {observer_username}', 404
        observer_id = username_to_uuid[observer_username]
        if observer_id not in users_list:
            return f'Could not find user {observer_username}', 404
        if observer_id not in users_to_observers[user_id]:
            return f'User {observer_username} already removed', 200
        users_to_observers[user_id].remove(observer_id)
        users_to_dependents[observer_id].remove(user_id)
        return f'User {observer_username} successfully removed', 200
    else:
        return 'Content-Type not supported!', 404


def check_user_id_exists(user_id):
    if user_id not in users_to_dependents:
        users_to_dependents[user_id] = []
    if user_id not in users_to_observers:
        users_to_observers[user_id] = []
    if user_id not in users_to_incoming_request:
        users_to_incoming_request[user_id] = []
    if user_id not in users_to_outgoing_request:
        users_to_outgoing_request[user_id] = []


@app.route('/explore', methods=['GET'])
def get_explore():
    user_id = request.args.get('user_id')
    if user_id is None:
        return 'User id must be provided', 400
    if user_id not in users_list:
        return f'User with id {user_id} not found', 404
    check_user_id_exists(user_id)

    result = [
        ('Dependents', users_to_dependents[user_id]),
        ('Observers', users_to_observers[user_id]),
        ('Incoming requests', users_to_incoming_request[user_id]),
        ('Outgoing requests', users_to_outgoing_request[user_id]),
    ]
    return flask.jsonify(
        [{'profiles': [profiles_to_json(user_info_to_profile(users_list[profile])) for profile in profiles],
          'list_name': list_name
          }
         for list_name, profiles in result]
    ), 200


if __name__ == '__main__':
    # run app in debug mode on port 5000
    app.run(debug=True, port=5000)
