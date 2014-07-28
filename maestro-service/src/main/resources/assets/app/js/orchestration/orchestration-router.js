'use strict';

angular.module('maestro')
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/orchestrations', {
        templateUrl: 'views/orchestration/orchestrations.html',
        controller: 'OrchestrationController',
        resolve:{
          resolvedOrchestration: ['Orchestration', function (Orchestration) {
            return Orchestration.query();
          }]
        }
      })
    }]);
