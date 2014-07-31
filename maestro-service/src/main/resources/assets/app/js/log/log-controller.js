'use strict';

angular.module('maestro')
  .controller('LogController', ['$scope', '$http', '$modal', 'resolvedLog', 'Log',
    function ($scope, $http, $modal, resolvedLog, Log) {

      $scope.logs = resolvedLog;

    }]);
