package controllers;

import play.api.Environment;
import play.mvc.*;
import play.data.*;
import play.db.ebean.Transactional;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import models.users.*;

public class LoginController extends Controller {

    private FormFactory formFactory;

    private Environment env;

    @Inject
    public LoginController(Environment e, FormFactory f){
        this.env = e;
        this.formFactory = f;
    }
}
