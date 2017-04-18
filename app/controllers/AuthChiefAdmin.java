package controllers;

import play.mvc.*;
import play.mvc.Action;
import java.util.concurrent.*;
import models.users.*;

public class AuthChiefAdmin extends Action.Simple {
    public CompletionStage<Result> call(Http.Context ctx){
        String id = ctx.session().get("numId");
        if(id != null){
            User u = User.getUserById(id);
            if("ChiefAdmin".equals(u.checkRole())){
                return delegate.call(ctx);
            }
        }
        ctx.flash().put("error", "Admin Login Required");
        return CompletableFuture.completedFuture(redirect(routes.HomeController.unauthorised()));
    }
}
