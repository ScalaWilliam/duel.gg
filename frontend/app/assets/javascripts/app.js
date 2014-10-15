define(function (require) {
    'use strict';
    'use strict';

    define([
        'angular',
        'filters',
        'services',
        'directives',
        'controllers',
        'angularRoute',
    ], function (angular, filters, services, directives, controllers) {

        // Declare app level module which depends on filters, and services

        return angular.module('myApp', [
            'ngRoute',
            'myApp.filters',
            'myApp.services',
            'myApp.directives',
            'myApp.controllers'
        ]);

    var angular = require('angular');

    var app = angular.module('likeastore');

    app.init = function () {
        angular.bootstrap(document, ['likeastore']);
    };

    app.config(['$routeProvider', '$locationProvider', '$httpProvider',
        function ($routeProvider, $locationProvider, $httpProvider) {
            $httpProvider.responseInterceptors.push('httpInterceptor');

            $locationProvider.html5Mode(true);
        }
    ]);

    app.run(function ($window, auth, user) {
        auth.setAuthorizationHeaders();
        console.log("Wut");
        user.initialize();
    });

    return app;
});
