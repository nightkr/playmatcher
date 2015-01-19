m = angular.module('playmatcher', [])

m.filter "if", ->
  (input, then_, else_) ->
    if (input)
      then_
    else
      else_

m.controller('GamesEditorController', [
  '$scope'
  '$http'

  ($scope, $http) ->
    @updateGameEnabled = (game) ->
      $http.post(jsRouter.controllers.Customization.setEnabled(game.game.id, game.enabled).url)
    @setAllGamesEnabled = (enable) ->
      for game in $scope.games
        game.enabled = enable
      $http.post(jsRouter.controllers.Customization.setAllEnabled(game.enabled).url)
    $http.get(jsRouter.controllers.Customization.myGames().url).success (response) ->
      $scope.games = response
])
