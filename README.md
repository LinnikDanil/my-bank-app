Задачи:

1) Реализовать Spring Cloud Gateway
2) Реализовать Service Discovery Consul
3) Реализовать Externalized в Consul
4) Добавить keycloak и realm-export к нему
5) Миркосервис фронт: Добавить openApi
6) микросервис аккаунтов (Accounts) OpenApi Spec
7) микросервис обналичивания денег (Cash) OpenApi Spec
8) микросервис перевода денег на счёт другого аккаунта (Transfer) OpenApi Spec
9) микросервис уведомлений (Notifications) OpenApi Spec
10) убрать template из setting.gradle


front:
авторизируется по Authorization Code Flow и ходит во все сервисы, кроме уведомлений


account:
таблица user:

* username [IvanIvanon]
* fullname [Иван Иванов]
* date_of_birth [10.05.2001]
* money_amount [10.11]

Микросервис Accounts авторизуется на OAuth 2.0 по Client Credentials Flow и ходит в notification


Cash:
Снятие и пополнение счета



Transfer:
Переводы между пользователями



История операций не была прописана в ТЗ, поэтому вынесена в логи/уведомления. При необходимости её можно добавить отдельными таблицами операций
