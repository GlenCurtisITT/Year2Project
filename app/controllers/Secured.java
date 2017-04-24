package controllers;

import controllers.*;
import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;
import models.users.*;

public class Secured extends Security.Authenticator{
    @Override
    public String getUsername(Context ctx){
        return ctx.session().get("numId");
    }

    @Override
    public Result onUnauthorized(Context ctx){
        return redirect(controllers.routes.HomeController.unauthorised());
    }
}
