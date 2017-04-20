package controllers;

import play.mvc.*;
import play.mvc.Action;
import java.util.concurrent.*;
import models.users.*;

/**
 * Created by Glen on 16/03/2017.
 */
public class AuthAdmin extends Action.Simple {
    public CompletionStage<Result> call(Http.Context ctx){
        String id = ctx.session().get("numId");
        if(id != null){
            User u = User.getUserById(id);
            if("Admin".equals(u.checkRole())){
                return delegate.call(ctx);
            }
        }
        ctx.flash().put("error", "Admin Login Required");
        return CompletableFuture.completedFuture(redirect(routes.HomeController.unauthorised()));
    }
}
