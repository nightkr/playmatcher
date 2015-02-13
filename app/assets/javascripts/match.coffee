m = angular.module('match', ['common'])

m.controller('MatchingController', [
  '$scope'

  ($scope) ->
    $scope.state = "searching"
    $scope.searchThreshold = 100
    connection = new WebSocket(jsRouter.controllers.Application.connectWS().webSocketURL() + location.search)

    connection.onerror = (err) ->
      console.log("WS error:", err)

    connection.onmessage = (e) ->
      msg = JSON.parse(e.data)
      switch msg.event
        when "new threshold"
          $scope.searchThreshold = msg.threshold
          $scope.$apply()
        when "match found"
          $scope.state = "matchFound"
          $scope.match = msg.person
          $scope.games = msg.person.games
          $scope.$apply()
        else
          console.warn("Unknown message received", msg)
])


