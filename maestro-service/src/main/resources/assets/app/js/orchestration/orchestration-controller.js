'use strict';

angular.module('maestro')
  .controller('OrchestrationController', ['$scope', '$http', '$modal', 'resolvedOrchestration', 'Orchestration', 'OutboundEndpoint',
    function ($scope, $http, $modal, resolvedOrchestration, Orchestration, OutboundEndpoint) {

      $scope.modes = ['Groovy', 'JavaScript', 'Python', 'Ruby'];

      $scope.orchestrations = resolvedOrchestration;

      $scope.getProperties = function (outboundEndpoint) {
        var props =  "";
        for (var propName in outboundEndpoint.properties) {
          props = props + propName + ": " + outboundEndpoint.properties[propName] + "\n";
        }
        outboundEndpoint.outboundProperties = props;
      };

      $scope.setProperties = function (outboundEndpoint) {
        var props =  outboundEndpoint.outboundProperties.split(/:?\s+/);
        outboundEndpoint.properties = {};
        for (var i = 0; i < props.length; i++) {
          if (i + 1 < props.length) {
            outboundEndpoint.properties[props[i]] = props[++i];
          }
        }
        delete outboundEndpoint.outboundProperties;
      };

      $scope.setOutboundEndpoint = function (outboundEndpoint) {
        $scope.getProperties(outboundEndpoint);
        $scope.outboundEndpoint = outboundEndpoint;
      };

      $scope.createOrchestration = function () {
        $scope.clearOrchestration();
        $scope.openOrchestration();
      };

      $scope.createOutboundEndpoint = function () {
        $scope.clearOutboundEndpoint();
        $scope.openOutboundEndpoint();
      };

      $scope.updateOrchestration = function (id) {
        Orchestration.get({id: id},
          function (data) {
            $scope.orchestration = data;
          }
        );
        $scope.openOrchestration(id);
      };

      $scope.updateOutboundEndpoint = function (id) {
        OutboundEndpoint.get({id: id},
          function (data) {
            $scope.setOutboundEndpoint(data);
          }
        );
        $scope.openOutboundEndpoint(id);
      };

      $scope.deleteOrchestration = function (id) {
        Orchestration.delete({id: id},
          function () {
            // TODO
            //$scope.orchestrations = Orchestration.query();
          });
      };

      $scope.deleteOutboundEndpoint = function (id) {
        OutboundEndpoint.delete({id: id},
          function () {
            // TODO
            //$scope.orchestrations = Orchestration.query();
          });
      };

      $scope.saveOrchestration = function (id) {
        if (id) {
          Orchestration.update({id: id}, $scope.orchestration,
            function (data) {
              $scope.orchestration = data;
            });
        } else {
          Orchestration.save($scope.orchestration,
            function (data) {
              $scope.orchestration = data;
            });
        }
      };

      $scope.saveOutboundEndpoint = function (id) {
        if (id) {
          $scope.setProperties($scope.outboundEndpoint);
          OutboundEndpoint.update({id: id}, $scope.outboundEndpoint,
            function (data) {
              $scope.setOutboundEndpoint(data);
            });
        } else {
          $scope.setProperties($scope.outboundEndpoint);
          OutboundEndpoint.save($scope.outboundEndpoint,
            function (data) {
              $scope.setOutboundEndpoint(data);
            });
        }
      };

      $scope.clearOrchestration = function () {
        $scope.orchestration = {
          "name": "",
          "contextPath": "",
          "relativePathTemplate": "",
          "contentType": "application/json",
          "method": "GET",
          "keepAlive": false,
          "script": "",
          "scriptType": "JavaScript"
        };
      };

      $scope.clearOutboundEndpoint = function () {
        $scope.outboundEndpoint = {
          "name": "",
          "type": "HTTP",
          "variableName": "",
          "outboundProperties": "",
          "properties": {},
          "script": "",
          "scriptType": "JavaScript"
        };
      };

      $scope.openOrchestration = function (id) {
        var orchestrationSave = $modal.open({
          templateUrl: 'orchestration-save.html',
          controller: OrchestrationSaveController,
          size: 'lg',
          resolve: {
            orchestration: function () {
              return $scope.orchestration;
            }
          }
        });

        orchestrationSave.result.then(function (orchestration) {
          $scope.orchestration = orchestration;
          $scope.saveOrchestration(id);
        });
      };

      $scope.openOutboundEndpoint = function (id) {
        var outboundEndpointSave = $modal.open({
          templateUrl: 'outbound-endpoint-save.html',
          controller: OutboundEndpointSaveController,
          size: 'lg',
          resolve: {
            outboundEndpoint: function () {
              return $scope.outboundEndpoint;
            }
          }
        });

        outboundEndpointSave.result.then(function (outboundEndpoint) {
          outboundEndpoint.orchestrationId = $scope.parentId;
          $scope.outboundEndpoint = outboundEndpoint;
          $scope.saveOutboundEndpoint(id);
        });
      };

      $scope.clearOrchestration();
      $scope.clearOutboundEndpoint();

      $scope.orchestrationHandler = function (orch) {
        $scope.mode = 'orchestration';
        $scope.orchestration = orch;
      };

      $scope.outboundEndpointHandler = function (orch, endpoint) {
        $scope.mode = 'outboundEndpoint';
        $scope.parentId = orch.id;
        $scope.setOutboundEndpoint(endpoint);
      };

      $scope.scriptHandler = function (orch) {
        $scope.mode = 'script';
        $scope.editMode = false;
        $scope.orchestration = Orchestration.get({id: orch.id});
      };

      $scope.deploy = function () {
        if ($scope.orchestration.state == 'Started') {
          $http.post('maestro/orchestrations/' + $scope.orchestration.id + '/stop', '').success(
            function (data, status) {
              $scope.orchestration.state = data.state;
            });
        } else {
          $http.post('maestro/orchestrations/' + $scope.orchestration.id + '/start', '').success(
            function (data, status) {
              $scope.orchestration.state = data.state;
            });
        }
      }

      $scope.edit = function () {
        if ($scope.editMode) {
          Orchestration.update({id: $scope.orchestration.id}, $scope.orchestration);
        }
        $scope.editMode = !$scope.editMode;
      }

      // The ui-ace options
      $scope.headersOption = {
        mode: 'properties',
        onLoad: function (editor) {
          //editor.setKeyboardHandler("ace/keyboard/vim");
          editor.setOptions({
            fontSize: "10pt"
          });
        }
      };
      $scope.payloadOption = {
        mode: $scope.outboundEndpoint.scriptType.toLowerCase(),
        onLoad: function (editor) {
          //editor.setKeyboardHandler("ace/keyboard/vim");
          editor.setOptions({
            fontSize: "10pt"
          });

          // HACK to have the ace instance in the scope...
          $scope.payloadModeChanged = function () {
            editor.getSession().setMode("ace/mode/" + $scope.outboundEndpoint.scriptType.toLowerCase());
          };
        }
      };
      $scope.outputOption = {
        mode: $scope.orchestration.scriptType.toLowerCase(),
        onLoad: function (editor) {
          //editor.setKeyboardHandler("ace/keyboard/vim");
          editor.setOptions({
            fontSize: "10pt"
          });

          // HACK to have the ace instance in the scope...
          $scope.outputModeChanged = function () {
            editor.getSession().setMode("ace/mode/" + $scope.orchestration.scriptType.toLowerCase());
          };
        }
      };

    }]);

var OrchestrationSaveController =
  function ($scope, $modalInstance, orchestration) {
    $scope.orchestration = orchestration;


    $scope.createdDateOptions = {
      dateFormat: 'yy-mm-dd'
    };
    $scope.lastModifiedDateOptions = {
      dateFormat: 'yy-mm-dd'
    };

    $scope.ok = function () {
      $modalInstance.close($scope.orchestration);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  };

var OutboundEndpointSaveController =
  function ($scope, $modalInstance, outboundEndpoint) {
    $scope.modes = ['Groovy', 'JavaScript', 'Python', 'Ruby'];

    $scope.outboundEndpoint = outboundEndpoint;

    $scope.createdDateOptions = {
      dateFormat: 'yy-mm-dd'
    };
    $scope.lastModifiedDateOptions = {
      dateFormat: 'yy-mm-dd'
    };

    $scope.ok = function () {
      $modalInstance.close($scope.outboundEndpoint);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  };
