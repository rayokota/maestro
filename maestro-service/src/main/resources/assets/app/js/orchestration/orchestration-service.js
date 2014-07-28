'use strict';

angular.module('maestro')
  .factory('Orchestration', ['$resource', function ($resource) {
    return $resource('maestro/orchestrations/:id', {}, {
      'query': { method: 'GET', isArray: true},
      'get': { method: 'GET'},
      'update': { method: 'PUT'}
    });
  }]);
