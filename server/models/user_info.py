from dataclasses import dataclass


@dataclass
class UserInfo:
    fullname: str
    username: str
    password: str
