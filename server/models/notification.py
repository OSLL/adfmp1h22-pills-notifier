from dataclasses import dataclass


@dataclass
class Notification:
    message: str
    date: str