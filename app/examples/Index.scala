package examples

import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.mvc._
import lib.IFUser




object Index extends Controller with AllViews 
    with ScalatagsResponseEncodings
  {

  // scalatag rendering features
  // - [ ] Simple mixins for layouts, common snippets, and scalatag imports
  // - [ ] low/high implicits picked up correctly
  // - [ ] "dynamic" views working properly

  /** As seen from object Index, the missing signatures are as follows.
    *  For convenience, these are usable as stub implementations.
    */
  // Members declared in lib.IFScalaTags
  def globalArgs: Seq[(String, Any)] = Seq()
  
  // Members declared in lib.IFUserLoginState
  def user: lib.IFUser[java.util.UUID] = null
  def userIsLoggedIn: Boolean = false
  def userOpt: Option[lib.IFUser[java.util.UUID]] = None
  
  // Members declared in lib.IFUserRequestState
  def request: play.api.mvc.RequestHeader = ???
  def requestOpt: Option[play.api.mvc.RequestHeader] = ???

  def index() = Action { req =>
    Ok(
      layoutV(
        splashV()
      )(Seq(
        "request" -> Some(req) // TODO use implicit here?
      ))
    )
  }
}


// import net.openreview.model.users._
// import lib.UserMapper._
// import java.util.UUID

import scalatags.Text.Frag
import _root_.lib._
import java.util.UUID

trait ViewCommonDefs
    extends IFScalaTags
    //with IFUserRequestState
    //with IFUserLoginState[UUID]
    with ajax.AjaxModule
{


  def openreviewLogo: Seq[Frag] = Seq(
    *.span("logo".cl, "green".cl)("open"),
    *.span("logo".cl, "blue".cl) ("review"),
    *.span("logo".cl, "green".cl)(".net")
  )

}

trait ScalatagsResponseEncodings {

  import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
  import scalatags.Text.Frag
  
  implicit def writeableOf_Frag(implicit codec: Codec): Writeable[Frag] =
    Writeable[Frag]((element: Frag) => {
      codec.encode(element.render)
    })


  implicit def contentTypeOf_Frag(implicit codec: Codec): ContentTypeOf[Frag] =
    ContentTypeOf[Frag](Some(ContentTypes.HTML))
}



trait LayoutViews extends ViewCommonDefs {
  import scalatags.Text.tags._
  import scalatags.Text.attrs._

  // JADE FILE: ./status/SessionMessage.index.jade
  def indexV(it:SessionMessage)(
    implicit args: Seq[(String, Any)]
  ): Frag = {
    ".span4".d.offset4.center.flash.alert(it.level.key.cl)(
      it.message,
      button.close(`type`:="button", data_dismiss:="alert")(
        *.i.icon_remove
      )
    )
  }

  def sessionMessages: Seq[SessionMessage] = Seq()



  // JADE FILE: ./layout/layout.jade
  def layoutV(mainContent: Frag, req: Option[RequestHeader]=None)(
    implicit args: Seq[(String, Any)]
  ): Frag = {
    def venueNavbar = {
      ".nav".d(style:="padding-top: 7px; padding-bottom: 5;")(
        (if(userIsLoggedIn) {
          // nav.nav.userSiteMap(request)
        } else ()),
        li.dropdown(
          a.dropdown_toggle(data_toggle:="dropdown")(
            "Venues",
            b.caret
          ),
          ul.dropdown_menu(
            // TODO example dropdown menu here
            //for (v <- lib.ORUserControllerGlobals.majorVenues(userOpt).toList) yield {
            //  li(
            //    a(href:=s"/venue/${v.venuePoV.venueInfo.slug}")(v.venuePoV.venueInfo.shortName.s)
            //  )
            //})
          )
            // TODO example sitemap here
            // nav.nav.siteMap(requestOpt.orElse(req).getOrElse(sys.error("no request available")))
        )
      )
    }

    def flashMessages = {
      ".flash-message".d(
        sessionMessages map indexV,
        "noscript".t(
          // TODO this is output as escaped html for some reason:
          //(new SessionMessage(Warning, "OpenReview requires javascript")) |> indexV
        )
      )
    }
 
    def loginWidget = {
      ".pull-right.vcentered".sp(
        userOpt.map({n =>
          loggedInV()
          // indexV(LoggedInNav(n))
        }).getOrElse({
          loggedOutV()
          //indexV(LoggedOutNav())
        })
      )
    }
    
  // JADE FILE: ./layout/LoggedOutNav.index.jade
    // def indexV(it:LoggedOutNav)(
  def loggedOutV()(
    implicit args: Seq[(String, Any)]
  ): Frag = {
    val hideLoginButton: Boolean = anyParam("hideLoginButton")(args) getOrElse false
    Seq(
      (if (!hideLoginButton) {
        ".btn-group.tmp".d(
          a(".loginbtn.btn.btn-primary".atts, href:="/login")("Login")
        )
      } else {
        ".hidden".d
      }),
      script(raw(
        """|returnTo = location.href.replace(/^http.?:\/\/[^/]+\//, '/');
           |$(".loginbtn").attr('href', '/secure'+returnTo);
           |""".stripMargin
      ))
    )
  }

  // JADE FILE: ./layout/LoggedInNav.index.jade
  def loggedInV()(
    implicit args: Seq[(String, Any)]
  ): Frag = {
    Seq(
      //if(it.user.isRoot) {
      //  a(href:="/admin")("Root Admin")
      //} else ".hidden".d,
      
      ".btn-group".d(
        a(".btn.btn-primary".atts, data_toggle:="dropdown", href:="/profile")(
          // (it.user.fullname getOrElse it.user.username).s
          "Mock T. Name"
        )// ,
        //button.btn.dropdown_toggle(s"btn-${it.dropdownIconLevel}".cl, data_toggle:="dropdown")(
        //  span.caret
        //),
        // 
        //ul.dropdown_menu(
        //  li(
        //    h5(style:="padding-left: 1em;")("Notifications")
        //  ),
        //  for (item <- it.notifications) yield {
        //    li(
        //      a(href:=s"${item.url.toString}")(
        //        span.badge(s"badge_${item.level}".cl)(
        //          s"${item.count}"
        //        ),
        //        s"${item.message}"
        //      )
        //    )
        //  },
        //  li.divider,
        //  li(
        //    a(href:="/profile")("Profile")
        //  ),
        //  li(
        //    a(href:="/logout")("Logout")
        //  )
        //)
      )
    )
  }

    // !!!5
    
    def navbar = {
      ".navbar.navbar-fixed-top".d(
        ".navbar-inner".d(
          ".container-fluid".d(
            ".row-fluid".d(
              ".span12".d(
                a("brand".cl, *.href:="/")(
                  openreviewLogo
                ),
                venueNavbar,
                flashMessages,
                loginWidget
              )
            )
          )
        )
      )
    }

    def footer = {
 
      "footer".t(".footer.center".atts)(
        ".row-fluid".d(
          ".span10.offset1".d(
            "address".t("center".cl)(
              p(
                a("mailto".att:="info@openreview.net") ("info@openreview.net")
              )
            ),
            "address".t(".center".atts)(
              p(
                openreviewLogo,
                " created by the ",
                a(href:="http://iesl.cs.umass.edu")("Information Extraction and Synthesis Laboratory"),
                " , School of Computer Science, ",
                " University of Massachusetts Amherst."
              ),
              p ("This work is supported in part by Google, NSF, and the Center for Intelligent Information Retrieval at the University of Massachusetts.")
            )
          )
        )
      )
    }
 
    val res = html(*.xmlns:="http://www.w3.org/1999/xhtml", "xml:lang".att:="en", *.lang:="en")(
      head(
        "title".t("openreview.net"),
        meta("http-equiv".att:="content-type", "content".att:="text/html; charset=UTF-8;"),
        meta(*.name:="viewport", "content".att:="width=device-width, initial-scale=1.0"),
 
        cssV("bootstrap-spacelab/css/bootstrap.css"),
        css("http://fonts.googleapis.com/css?family=Lato"),
        cssV("stylesheets/application.css"),
        cssV("jquery-ui-1.10.3.custom/css/flick/jquery-ui-1.10.3.custom.min.css")
      ),
      body(
        jsV("javascripts/jquery-1.10.2.min.js"),
        jsV("jquery-ui/js/jquery-ui-1.10.3.custom.min.js"),
        jsV("bootstrap-spacelab/js/bootstrap.js"),
        jsV("javascripts/spin.js"),
        jsV("javascripts/jquery.spin.js"),
        jsV("javascripts/eldarion-ajax-core.js"),
        jsV("javascripts/eldarion-ajax-handlers.js"),
        jsV("javascripts/iesl-frame.js"),
        jsV("javascripts/application.js"),
        
        "#wrap".d(
          "#main.container-fluid.clear-top".d(
            navbar,
            ".container-fluid.below-navbar".d(
              row_fluid(
                ".span12".d(
                  mainContent
                )
              ),
              row_fluid(".hbar-red".d)
            )
          )
        ),
        footer
      )
    )
    println("end:layoutV")
    res
  }
 
 
  // JADE FILE: ./splash.jade
  def splashV() = {
    div(
      row_fluid(
        span12(
          h1("logo".cl, "center".cl)(
            openreviewLogo
          ),
          h2("logo".cl, "center".cl, "green".cl)("Open Reviewing Network")
        )
      ),
      br,
      br,
      br,
      br,
      row_fluid.vcentered(
        span8.offset2(
          well(
            ".lead.center".d(raw(
              s"""|Paper submission, reviewing, and public discussion for
                  | ${a(href:="/iclr2014") ("ICLR 2014")},
                  | ${a(href:="/iclr2013") ("ICLR 2013")},
                  | ${a(href:="/inferning2013") ("ICML/Inferning 2013")},
                  | ${a(href:="/icml-peer2013") ("ICML/Peer Review 2013")},
                  | and ${a(href:="/akbc2013") ("AKBC 2013")} """.stripMargin)
            )
          )
        )
      )
    )
  }

}

trait SplashViews {
}

trait IFUserAccountViews {

}


trait AllViews
    extends LayoutViews
    with SplashViews
    with IFUserAccountViews {

}
