'use strict';

angular.module('maestro')
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/logs', {
        templateUrl: 'views/log/logs.html',
        controller: 'LogController',
        resolve:{
          resolvedLog: ['Log', function (Log) {
            return Log.query();
          }]
        }
      })
    }]);
