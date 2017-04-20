package controllers;

import models.users.User;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by conno on 20/04/2017.
 */
public class AuthConsultant extends Action.Simple {
    public CompletionStage<Result> call(Http.Context ctx){
        String id = ctx.session().get("numId");
        if(id != null){
            User u = User.getUserById(id);
            if("Consultant".equals(u.checkRole())){
                return delegate.call(ctx);
            }
        }
        ctx.flash().put("error", "Consultant Login Required");
        return CompletableFuture.completedFuture(redirect(routes.HomeController.unauthorised()));
    }
}
