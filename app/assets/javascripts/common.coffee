m = angular.module('common', [])

m.filter "if", ->
  (input, then_, else_) ->
    if (input)
      then_
    else
      else_
