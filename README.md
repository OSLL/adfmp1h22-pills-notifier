# adfmp1h22-pills-notifier

## Запуск сервера с помощью Docker
Выполняем команду
```
docker build -t pill-reminder-server -f server.Dockerfile .
```
для создания образа. Далее запускаем контейнер с помощью
```
docker run -it pill-reminder-server
```
Из появившегося окна копируем ссылку вида https://c86b-178-71-107-132.ngrok.io и 
вставляем в [сom.example.pillnotifier.Constants.BASE_URL](https://github.com/OSLL/adfmp1h22-pills-notifier/blob/server/app/src/main/java/com/example/pillnotifier/Constants.kt#L4)

## Запуск сервера вручную:
При первом использовании:
1) Устанавливаем [flask](https://flask.palletsprojects.com/en/2.0.x/) и
   [ngrok](https://ngrok.com/download):
   ` pip install flask ngrok`
2) если `ngrok` раннее не использовался, необходимо с помощью [этой](https://ngrok.com/docs#getting-started-authtoken)
инструкции  зарегистрироваться и задать  полученный токен с помощью команды

   `ngrok authtoken <YOUR_AUTHTOKEN>`

Далее:
1) в папке `server` запускаем:
  
   `flask run`
2) где-нибудь запускаем

   `ngrok http 5000`
3) из появившегося окна копируем ссылку вида http://c86b-178-71-107-132.ngrok.io и 
вставляем в [сom.example.pillnotifier.Constants.BASE_URL](https://github.com/OSLL/adfmp1h22-pills-notifier/blob/server/app/src/main/java/com/example/pillnotifier/Constants.kt#L4)

### Тестовый аккаунт
login: `test_user`,
password: `123456`
