package controllers;

import models.users.User;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by Glen on 16/03/2017.
 */
public class AuthAdminOrConsultant extends Action.Simple{
    public CompletionStage<Result> call(Http.Context ctx){
        String id = ctx.session().get("numId");
        if(id != null){
            User u = User.getUserById(id);
            if("Admin".equals(u.checkRole())){
                return delegate.call(ctx);
            }else if("Consultant".equals(u.checkRole())){
                return delegate.call(ctx);
            }
        }
        ctx.flash().put("error", "Admin/Consultant Login Required");
        return CompletableFuture.completedFuture(redirect(routes.HomeController.unauthorised()));
    }
}
