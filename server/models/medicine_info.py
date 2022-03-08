from dataclasses import dataclass
from datetime import datetime
from enum import Enum


class Regularity(str, Enum):
    DAILY = 'DAILY'
    ONCE_IN_TWO_DAYS = 'ONCE_IN_TWO_DAYS'
    ONCE_A_WEEK = 'ONCE_A_WEEK'


@dataclass
class MedicineInfo:
    medicine_name: str
    portion: str
    instructions: str
    regularity: Regularity
    start_date: datetime
    time: datetime
