# Arrival Service Platform Test Task for Clojure Developer

## Сервис регистрации заявок
Реализовать SPA приложение и RESTful сервер регистрации заявок.<br> 
Приложение должно поддерживать два сценария работы: создание заявки и отображение списка зарегистрированных заявок.<br>
Для каждой заявки должна быть возможность указать: 
* заголовок
* описание
* заявителя
* исполнителя
* дату исполнения заявки<br> 

Идентификаторы заявок должны генерироваться автоматически.<br>
Сервер необходимо реализовать на основе библиотек ring, compojure.<br> 
Клиент следует реализовать с использованием библиотек reagent и re-frame.<br>

Приложение должно собираться в один файл testapp.jar и содержать в себе сервер jetty.
Приложение должно запускаться командой
```
java -jar testapp.jar
``` 
После запуска веб-приложение должно быть доступно по ссылке http://localhost:8080/testapp

### Дополнительные требования для кандидатов, претендующих на разные уровни 
#### Junior
Сервер может хранить данные в памяти.
    
#### Middle    
Сервер должен хранить данные в базе данных Datomic.<br>

#### Senior
Все требованиям Middle.<br>
Обработка запросов сервером должна производится с помощью библиотеки core.async.<br>       

## Полезные ссылки
https://clojure.org - основной ресурс по Clojure<br>
https://github.com/ring-clojure - библиотека ring<br> 
https://github.com/weavejester/compojure - библиотека compojure<br> 
https://github.com/clojure/core.async - библиотека core.async<br>
https://github.com/eclipse/jetty.project - web-сервер jetty<br>
https://www.datomic.com/get-datomic.html - Datomic Database (Starter - Free)<br> 
https://reagent-project.github.io/ - библиотека reagent<br> 
https://github.com/Day8/re-frame - библиотека re-frame<br>
https://leiningen.org - инструментарий для создания проектов Clojure<br> 
https://cursive-ide.com - IDE для разработки на основе IntelliJ<br> 
https://cider.readthedocs.io/en/latest - IDE для разработки на основе Emacs
