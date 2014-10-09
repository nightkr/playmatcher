connection = new WebSocket(jsRoutes.controllers.Application.connectWS().webSocketURL() + location.search)

templates =
  searching: Handlebars.compile(Handlebars.Templates.Searching)
  matchFound: Handlebars.compile(Handlebars.Templates.Matchfound)

connection.onerror = (err) ->
  console.log("WS error:", err)

connection.onmessage = (e) ->
  msg = JSON.parse(e.data)
  switch msg.event
    when "new threshold"
      $("#body").html(templates.searching({
        threshold: msg.threshold
      }))
    when "match found"
      $("#body").html(templates.matchFound(msg.person))
    else
      console.warn("Unknown message received", msg)
