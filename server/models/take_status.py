from enum import Enum


class TakeStatus(str, Enum):
    TAKEN = 'TAKEN'
    NOT_TAKEN = 'NOT_TAKEN'
    UNKNOWN = 'UNKNOWN'
