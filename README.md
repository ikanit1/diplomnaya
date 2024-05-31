
# Описание приложения

## О проекте

Это приложение предназначено для управления задачами и позволяет пользователям создавать, редактировать, удалять и делиться задачами в группе. Приложение также поддерживает уведомления о задачах.

### Основные функции:

- **Создание задач:** Пользователи могут создавать задачи, указывая заголовок, описание, приоритет, необходимость уведомления, дату и время выполнения.
- **Редактирование задач:** Пользователи могут редактировать существующие задачи.
- **Удаление задач:** Пользователи могут удалять задачи.
- **Повторяющиеся задачи:** Поддержка создания повторяющихся задач с указанием дней недели и времени выполнения.
- **Уведомления:** Пользователи получают уведомления о задачах.
- **Деление задач с группой:** Пользователи могут делиться задачами с группами.

## Структура приложения

### Основные классы:

1. **WorkSpace.java**
    - Отвечает за отображение списка задач, создание новых задач и редактирование существующих задач.
    - Управляет уведомлениями и отображает задачи в соответствующем виде.

2. **NotificationHelper.java**
    - Управляет созданием и отправкой уведомлений о задачах.
    - Используется для отправки уведомлений пользователям, когда создаются или обновляются задачи.

3. **TaskDatabaseHelper.java**
    - Отвечает за взаимодействие с базой данных Firebase.
    - Обрабатывает добавление, обновление и удаление задач.
    - Распространяет задачи среди членов группы и управляет состоянием выполнения задач.

4. **OwnRoom.java**
    - Класс, который отвечает за отображение пользовательской комнаты, где пользователь может управлять своими личными настройками и задачами.

5. **Register.java**
    - Класс для регистрации новых пользователей.
    - Обрабатывает ввод данных, валидацию и создание новых учетных записей в Firebase.

6. **ShareTask.java**
    - Класс для деления задач с другими пользователями и группами.
    - Позволяет пользователям выбирать группы и делиться с ними своими задачами.

7. **Task.java**
    - Модель данных задачи.
    - Содержит информацию о заголовке, описании, приоритете, необходимости уведомлений, дате и времени выполнения, статусе выполнения и других параметрах задачи.

## Настройка и запуск

1. **Клонирование репозитория:**
    ```bash
    git clone <URL_репозитория>
    cd <название_репозитория>
    ```

2. **Настройка Firebase:**
    - Создайте проект в Firebase.
    - Настройте аутентификацию Firebase и базу данных Realtime Database.
    - Добавьте файл `google-services.json` в папку `app` вашего проекта.

3. **Запуск приложения:**
    - Откройте проект в Android Studio.
    - Синхронизируйте проект с Gradle.
    - Запустите приложение на устройстве или эмуляторе.

## Примечание

Убедитесь, что у вас установлены все необходимые зависимости и настройки для корректной работы Firebase в вашем проекте.

## Лицензия

Этот проект лицензирован под лицензией MIT. См. файл LICENSE для подробностей.
