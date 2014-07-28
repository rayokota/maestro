'use strict';

angular.module('maestro')
  .factory('OutboundEndpoint', ['$resource', function ($resource) {
    return $resource('maestro/outboundEndpoints/:id', {}, {
      'query': { method: 'GET', isArray: true},
      'get': { method: 'GET'},
      'update': { method: 'PUT'}
    });
  }]);
