package controllers

object PropertyCustomization extends PMController {
  def myProperties = APIUserAction { rq =>
    implicit val (req, session, user) = rq
    Ok("Hi")
  }
}
