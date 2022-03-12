from dataclasses import dataclass
from datetime import date, time, timedelta
from enum import Enum


class Regularity(str, Enum):
    DAILY = 'DAILY'
    ONCE_IN_TWO_DAYS = 'ONCE_IN_TWO_DAYS'
    ONCE_A_WEEK = 'ONCE_A_WEEK'

    def take_dates_generator(self, start_date: date, end_date:date):
        delta_days = 1 if self is Regularity.DAILY else 2 if self is Regularity.ONCE_IN_TWO_DAYS else 7
        for n in range(0, int((end_date - start_date).days) + 1, delta_days):
            yield start_date + timedelta(n)


@dataclass
class MedicineInfo:
    medicine_name: str
    portion: str
    regularity: Regularity
    start_date: date
    end_date: date
    time: time
