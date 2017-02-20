package controllers;

import controllers.*;
import play.mvc.*;

import views.html.*;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import play.data.*;

import javax.inject.Inject;
import models.users.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private FormFactory formFactory;

    @Inject
    public HomeController(FormFactory f){
        this.formFactory = f;
    }

    public Result index() {
        return ok(index.render());
    }

    public Result homepage(){
        return ok(homepage.render());
    }

    public Result createUser(){
        Form<User> adduserForm = formFactory.form(User.class);
        return ok(createUser.render(adduserForm));
    }

    public Result addUserSubmit(){
        DynamicForm newUserForm = formFactory.form().bindFromRequest();
        if(newUserForm.hasErrors()){
            Form<User> errorForm = formFactory.form(User.class);
            return badRequest(createUser.render(errorForm));
        }

        User.create(newUserForm.get("email"), newUserForm.get("role"), newUserForm.get("name"), newUserForm.get("password"));
        //User.create("test", "test", "test", "test");
        return redirect(controllers.routes.HomeController.index());
    }
}
