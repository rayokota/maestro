'use strict';

angular.module('maestro')
  .factory('Log', ['$resource', function ($resource) {
    return $resource('maestro/logs/:id', {}, {
      'query': { method: 'GET', isArray: true},
      'get': { method: 'GET'},
      'update': { method: 'PUT'}
    });
  }]);
