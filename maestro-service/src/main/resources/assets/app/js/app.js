// Declare app level module which depends on filters, and services
angular.module('maestro', ['ngResource', 'ngRoute', 'ui.bootstrap', 'ui.date', 'angularBootstrapNavTree', 'ui.ace'])
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/home/home.html', 
        controller: 'HomeController'})
      .otherwise({redirectTo: '/'});
  }]);
