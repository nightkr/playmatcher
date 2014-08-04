connection = new WebSocket(jsRoutes.controllers.Application.connectWS().webSocketURL() + location.search)

templates =
  matchFound: Handlebars.compile(Handlebars.Templates.Matchfound)

connection.onerror = (err) ->
  console.log("WS error:", err)

connection.onmessage = (e) ->
  msg = JSON.parse(e.data)
  switch msg.event
    when "match found"
      console.log(msg.person)
      $("#body").html(templates.matchFound(msg.person))
    else
      console.warn("Unknown message received", msg)
