'use strict';

angular.module('maestro')
  .controller('OrchestrationController', ['$scope', '$http', '$modal', 'resolvedOrchestration', 'Orchestration', 'OutboundEndpoint',
    function ($scope, $http, $modal, resolvedOrchestration, Orchestration, OutboundEndpoint) {

      $scope.modes = ['Groovy', 'JavaScript', 'Python', 'Ruby'];

      $scope.orchestrations = resolvedOrchestration;

      $scope.getProperties = function (outboundEndpoint) {
        console.log(JSON.stringify(outboundEndpoint.properties));
        var props =  "";
        for (var propName in outboundEndpoint.properties) {
          props = props + propName + ": " + outboundEndpoint.properties[propName] + "\n";
        }
        outboundEndpoint.outboundProperties = props;
      }

      $scope.setProperties = function (outboundEndpoint) {
        console.log(JSON.stringify(outboundEndpoint.outboundProperties));
        var props =  outboundEndpoint.outboundProperties.split(/:?\s+/);
        outboundEndpoint.properties = {};
        for (var i = 0; i < props.length; i++) {
          if (i + 1 < props.length) {
            outboundEndpoint.properties[props[i]] = props[++i];
          }
        }
        delete outboundEndpoint.outboundProperties;
      }

      $scope.setOutboundEndpoint = function (outboundEndpoint) {
        $scope.getProperties(outboundEndpoint);
        $scope.outboundEndpoint = outboundEndpoint;
      }

      resolvedOrchestration.$promise.then(function (data) {
        var treedata = [ ];

        for (var i = 0; i < data.length; i++) {
          var orch = data[i];
          var childrenData = [ ];
          for (var j = 0; j < orch.outboundEndpoints.length; j++) {
            var endpoint = orch.outboundEndpoints[j];
            $scope.getProperties(endpoint);
            childrenData[childrenData.length] = {
              label: endpoint.name,
              data: {
                type: 'outboundEndpoint',
                parentId: endpoint.orchestrationId,
                outboundEndpoint: endpoint
              }
            }
          }
          delete orch.outboundEndpoints;
          treedata[treedata.length] = {
            label: orch.name,
            data: {
              type: 'orchestration',
              parentId: 0,
              orchestration: orch
            },
            children: [
              {
                label: 'Inputs',
                data: {
                  type: 'outboundEndpoints',
                  parentId: orch.id
                },
                children: childrenData
              },
              {
                label: 'Output',
                data: {
                  type: 'script',
                  parentId: orch.id
                },
                children: []
              }
            ]
          };
        }
        $scope.my_data = [
          { label: 'Orchestrations',
            data: {
              type: 'orchestrations'
            },
            children: treedata
          }
        ];
      });

      $scope.create = function () {
        $scope.clear();
        $scope.open();
      };

      $scope.update = function (id) {
        if ($scope.mode == 'orchestrations') {
          $scope.orchestration = Orchestration.get({id: id});
        } else if ($scope.mode == 'outboundEndpoints') {
          $scope.setOutboundEndpoint(OutboundEndpoint.get({id: id}));
        }
        $scope.open(id);
      };

      $scope.delete = function (id) {
        Orchestration.delete({id: id},
          function () {
            // TODO
            //$scope.orchestrations = Orchestration.query();
          });
      };

      $scope.save = function (id) {
        if ($scope.mode == 'orchestrations' || $scope.mode == 'orchestration') {
          if (id) {
            Orchestration.update({id: id}, $scope.orchestration,
              function (data) {
                $scope.orchestration = data;
              });
          } else {
            Orchestration.save($scope.orchestration,
              function (data) {
                $scope.orchestration = data;
                $scope.add_branch();
              });
          }
        } else if ($scope.mode == 'outboundEndpoints' || $scope.mode == 'outboundEndpoint') {
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
                $scope.add_branch();
              });
          }

        }
      };

      $scope.clear = function () {
        if ($scope.mode == 'orchestrations') {
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
        } else if ($scope.mode == 'outboundEndpoints') {
          $scope.outboundEndpoint = {
            "name": "",
            "type": "HTTP",
            "variableName": "",
            "outboundProperties": "",
            "properties": {},
            "script": "",
            "scriptType": "JavaScript"
          };
        }
      };

      $scope.open = function (id) {
        if ($scope.mode == 'orchestrations' || $scope.mode == 'orchestration') {
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
            $scope.save(id);
          });
        } else if ($scope.mode == 'outboundEndpoints' || $scope.mode == 'outboundEndpoint') {
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
            $scope.save(id);
          });

        }
      };

      $scope.mode = 'outboundEndpoints';
      $scope.clear();
      $scope.mode = 'orchestrations';
      $scope.clear();

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

      var tree;
      $scope.my_tree_handler = function (branch) {
        var _ref;
        $scope.mode = branch.data.type;
        $scope.parentId = branch.data.parentId;
        if ($scope.mode == 'orchestration') {
          $scope.orchestration = branch.data.orchestration;
        } else if ($scope.mode == 'outboundEndpoint') {
          $scope.setOutboundEndpoint(branch.data.outboundEndpoint);
        } else if ($scope.mode == 'script') {
          $scope.editMode = false;
          $scope.orchestration = Orchestration.get({id: $scope.parentId});
        }
      };
      $scope.my_data = [
        { label: "Orchestrations" }
      ];
      $scope.my_tree = tree = {};
      $scope.add_branch = function () {
        var b;
        b = tree.get_selected_branch();
        if ($scope.mode == 'orchestrations') {
          var orch = $scope.orchestration;
          return tree.add_branch(b, {
            label: orch.name,
            data: {
              type: 'orchestration',
              parentId: 0,
              orchestration: orch
            },
            children: [
              {
                label: 'Inputs',
                data: {
                  type: 'outboundEndpoints',
                  parentId: orch.id
                },
                children: []
              },
              {
                label: 'Output',
                data: {
                  type: 'script',
                  parentId: orch.id
                },
                children: []
              }
            ]
          });
        } else if ($scope.mode == 'outboundEndpoints') {
          var endpoint = $scope.outboundEndpoint;
          return tree.add_branch(b, {
            label: $scope.outboundEndpoint.name,
            data: {
              type: 'outboundEndpoint',
              parentId: endpoint.orchestrationId,
              outboundEndpoint: endpoint
            }
          });
        }
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
