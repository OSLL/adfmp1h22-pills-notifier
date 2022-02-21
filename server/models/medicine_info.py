from dataclasses import dataclass
from datetime import datetime
from enum import Enum


class Regularity(Enum):
    DAILY = 1
    ONCE_IN_TWO_DAYS = 2
    ONCE_A_WEEK = 3


@dataclass
class MedicineInfo:
    medicine_name: str
    portion: str
    instructions: str
    regularity: Regularity
    start_date: datetime
    time: datetime
