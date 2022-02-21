# import main Flask class and request object
from datetime import datetime
from flask import Flask, request
from typing import Dict
from models.medicine_info import Regularity, MedicineInfo

# create the Flask app
app = Flask(__name__)


user_to_medicine: Dict[str, MedicineInfo] = {}


@app.route('/medicine', methods=['POST'])
def add_medicine():
    content_type = request.headers.get('Content-Type')
    if content_type == 'application/json':
        json = request.json
        user_id = json['user_id']
        medicine_name = json['medicine_name']
        instructions = json['instructions']
        regularity = Regularity[json['regularity']]
        format_date = '%Y-%m-%d'
        start_date = datetime.strptime(json['date'], format_date)
        format_time = '%H:%M'
        time = datetime.strptime(json['time'], format_time)
        medicine_info = MedicineInfo(medicine_name=medicine_name, instructions=instructions, regularity=regularity,
                                     start_date=start_date, time=time)
        user_to_medicine[user_id] = medicine_info
        return 'OK'
    else:
        return 'Content-Type not supported!'


if __name__ == '__main__':
    # run app in debug mode on port 5000
    app.run(debug=True, port=5000)
