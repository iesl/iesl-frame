package lib

import com.typesafe.scalalogging.slf4j.Logging
import play.api.Play.current
import play.api.mvc._

import play.api.http.Status._
import play.api.http.Writeable
import play.api.http.HeaderNames._
import play.api.libs.iteratee.Enumerator
import play.api.mvc.Results.Status
import play.api.libs.json.JsValue
import lib.ajax._


import scalatags.{SeqModifier => _, _}

object custom extends CustomHtmlTags

//trait PrefixedHtmlTags extends Attrs with Styles with Tags with DataConverters with Logging {
trait PrefixedHtmlTags extends Logging {
  import scalatags._

  val * = new Attrs with Styles with Tags {}

}

// trait CustomHtmlTags extends Attrs with Styles with Tags with DataConverters with Logging {
trait CustomHtmlTags extends PrefixedHtmlTags {
  import scalaz.syntax.id._   // gives func syntax |>
  import scalatags._

  import *.{div, img, src}

  type HtmlTag = scalatags.HtmlTag
  type Node = scalatags.Node
  type Modifier = scalatags.Modifier

  def raw = scalatags.raw _

  case class IdAttr(name: String) extends Modifier {
    def transform(tag: HtmlTag) =
      tag(*.id := name)
  }

  case class Group(
    xs: Node*
  ) extends Node {
    override def transform(tag: HtmlTag) = {
      var newTag = tag

      var i = 0
      while(i < xs.length){
        newTag = xs(i).transform(newTag)
        i += 1
      }
      newTag
    }
    override def writeTo(strb: StringBuilder): Unit = {}
  }

  implicit def scalaXmlAdapter(x: scala.xml.Node): RawNode = {
    val prettier = new scala.xml.PrettyPrinter(180, 4)
    RawNode(prettier.format(x))
  }
  
  def parseAttribs(as: String): List[Modifier] = {
    def aname(ss: String): (String, String, String) = (
      ss.take(1),
      ss.drop(1).takeWhile(c => !("#." contains c)),
      ss.drop(1).dropWhile(c => !("#." contains c)))


    if (as.isEmpty()) List()
    else {
      val (t, a, r) = aname(as)
      t match {
        case "#" => a.id :: parseAttribs(r)
        case "." => a.cl :: parseAttribs(r)
        case _   => logger.error(s"parsing invalid attribute type $t' in $as at $r")
          parseAttribs(r)
      }
    }
  }


  implicit class EnrichedString(s: String) {
    def id = { if (s.startsWith("#")) sys.error("dont put a '#' in *.cl") else new IdAttr(s) } 
    def cl = { if (s.startsWith(".")) sys.error("dont put a '.' in *.cl") else Cls(s) }
    def atts = parseAttribs(s)
    def att = s.attr


    def t = s.tag
    def d = *.div(atts:_*)
    def p = *.p(atts:_*)
    def sp = *.span(atts:_*)
  }


  object noopNode extends Node {
    override def transform(tag: HtmlTag) = tag
    def writeTo(strb: StringBuilder): Unit = {}
  }


  val assetsVersion = 16

  def cssV(s: String) = css(s"/assets/$assetsVersion/$s")
  def jsV(s: String) = js(s"/assets/$assetsVersion/$s")

  implicit class EnrichedHtmlTag(t: HtmlTag) {
    def control_label      =  ".control-label".atts      |> t.apply
    def control_group      =  ".control-group".atts      |> t.apply
    def control_group_inline= ".control-group-inline".atts      |> t.apply
    def well               =  ".well".atts               |> t.apply 
    def offset1            =  ".offset1".atts            |> t.apply 
    def offset2            =  ".offset2".atts            |> t.apply 
    def offset3            =  ".offset3".atts            |> t.apply 
    def offset4            =  ".offset4".atts            |> t.apply 
    def offset5            =  ".offset5".atts            |> t.apply 
    def offset6            =  ".offset6".atts            |> t.apply 
    def offset7            =  ".offset7".atts            |> t.apply 
    def offset8            =  ".offset8".atts            |> t.apply 
    def offset9            =  ".offset9".atts            |> t.apply 
    def offset10           =  ".offset10".atts           |> t.apply 
    def offset11           =  ".offset11".atts           |> t.apply 
    def offset12           =  ".offset12".atts           |> t.apply 
    def span1              =  ".span1".atts              |> t.apply 
    def span2              =  ".span2".atts              |> t.apply 
    def span3              =  ".span3".atts              |> t.apply 
    def span4              =  ".span4".atts              |> t.apply 
    def span5              =  ".span5".atts              |> t.apply 
    def span6              =  ".span6".atts              |> t.apply 
    def span7              =  ".span7".atts              |> t.apply 
    def span8              =  ".span8".atts              |> t.apply 
    def span9              =  ".span9".atts              |> t.apply 
    def span10             =  ".span10".atts             |> t.apply 
    def span11             =  ".span11".atts             |> t.apply 
    def span12             =  ".span12".atts             |> t.apply 
    def form_horizontal    =  ".form-horizontal".atts    |> t.apply 
    def btn                =  ".btn".atts                |> t.apply 
    def btn_lightlink      =  ".btn-lightlink".atts      |> t.apply 
    def vcentered          =  ".vcentered".atts          |> t.apply 
    def center             =  ".center".atts             |> t.apply 
    def spanN(n:Int)       =  s".span$n".atts            |> t.apply 
    def flash              =  ".flash".atts              |> t.apply 
    def alert              =  ".alert".atts              |> t.apply 
    def alert_warning      =  ".alert-warning".atts      |> t.apply 
    def title              =  ".title".atts              |> t.apply 
    def close              =  ".close".atts              |> t.apply 
    def icon_remove        =  ".icon-remove".atts        |> t.apply 
    def icon_lock          =  ".icon-lock".atts          |> t.apply 
    def icon_unlock        =  ".icon-unlock".atts        |> t.apply 
    def icon_arrow_left    =  ".icon-arrow-left".atts    |> t.apply 
    def icon_arrow_right   =  ".icon-arrow-right".atts   |> t.apply 
    def icon_edit          =  ".icon-edit".atts          |> t.apply 
    def icon_globe         =  ".icon-globe".atts         |> t.apply 
    def icon_ok            =  ".icon-ok".atts            |> t.apply 
    def icon_play          =  ".icon-play".atts          |> t.apply 
    def icon_plus_sign     =  ".icon-plus-sign".atts     |> t.apply 
    def icon_question_sign =  ".icon-question-sign".atts |> t.apply 
    def icon_remove_sign   =  ".icon-remove-sign".atts   |> t.apply 
    def icon_share_alt     =  ".icon-share-alt".atts     |> t.apply 
    def icon_star          =  ".icon-star".atts          |> t.apply 
    def icon_star_empty    =  ".icon-star-empty".atts    |> t.apply 
    def icon_th_list       =  ".icon-th-list".atts       |> t.apply 
    def icon_user          =  ".icon-user".atts          |> t.apply 
    def well_small         =  ".well-small".atts         |> t.apply 
    def nav                =  ".nav".atts                |> t.apply 
    def nav_pills          =  ".nav-pills".atts          |> t.apply 
    def nav_stacked        =  ".nav-stacked".atts        |> t.apply 
    def eventstaterefresh  =  ".eventstaterefresh".atts  |> t.apply 
    def nav_header         =  ".nav-header".atts         |> t.apply 
    def right              =  ".right".atts              |> t.apply 
    def nowrap             =  ".nowrap".atts             |> t.apply 
    def badge              =  ".badge".atts              |> t.apply 
    def dropdown           =  ".dropdown".atts           |> t.apply 
    def dropdown_toggle    =  ".dropdown-toggle".atts    |> t.apply 
    def dropdown_menu      =  ".dropdown-menu".atts      |> t.apply 
    def badge_todo         =  ".badge-todo".atts         |> t.apply 
    def badge_blocked      =  ".badge-blocked".atts      |> t.apply 
    def badge_waiting      =  ".badge-waiting".atts      |> t.apply 
    def fixedbadge         =  ".fixedbadge".atts         |> t.apply 
    def requestcount       =  ".requestcount".atts       |> t.apply 
    def pdflink            =  ".pdflink".atts            |> t.apply 
    def withtooltip        =  ".withtooltip".atts        |> t.apply 
    def thinborder         =  ".thinborder".atts         |> t.apply 
    def ajax               =  ".ajax".atts               |> t.apply 
    def arxiv              =  ".arxiv".atts              |> t.apply 
    def date               =  ".date".atts               |> t.apply 
    def spacedout          =  ".spacedout".atts          |> t.apply 
    def fadeable           =  ".fadeable".atts           |> t.apply 
    def abztract           =  ".abztract".atts           |> t.apply 
    def pull_right         =  ".pull-right".atts         |> t.apply 
    def label              =  ".label".atts              |> t.apply 
    def label_success      =  ".label-success".atts      |> t.apply 
    def read_more          =  ".read-more".atts          |> t.apply 
    def read_more_toggle   =  ".read-more-toggle".atts   |> t.apply 
    def caret              =  ".caret".atts              |> t.apply 
    def divider            =  ".divider".atts            |> t.apply 
    def help_inline        =  ".help-inline".atts            |> t.apply 
    def h_indent_ul        =  ".h_indent_ul".atts            |> t.apply 
    def small        =  ".small".atts            |> t.apply 
    def logo        =  ".logo".atts            |> t.apply 
    def green        =  ".green".atts            |> t.apply 
    def blue        =  ".blue".atts            |> t.apply 

    def h1            =  ".h1".atts            |> t.apply 
    def h2            =  ".h2".atts            |> t.apply 
    def h3            =  ".h3".atts            |> t.apply 
    def h4            =  ".h4".atts            |> t.apply 
    def h5            =  ".h5".atts            |> t.apply 



  }

  def css(p: String) = *.link(*.rel:="stylesheet", *.`type`:="text/css", *.href:=p)
  def js(p: String) = *.script(*.src:=p)

  //def rowF = div("row".attr:="row-fluid")

  //def spanN(n:Int) = div(s"span$n".cls)
  def span1  = ".span1".d
  def span2  = ".span2".d
  def span3  = ".span3".d
  def span4  = ".span4".d
  def span5  = ".span5".d
  def span6  = ".span6".d
  def span7  = ".span7".d
  def span8  = ".span8".d
  def span9  = ".span9".d
  def span10 = ".span10".d
  def span11 = ".span11".d
  def span12 = ".span12".d

  def ajaxerror =      ".ajaxerror".d
  def expand =         ".expand".d
  def abstractsmall =  ".abstractsmall".d
  def abztract =       ".abstract".d


  def offset4 = ".offset4".d

  def prettyFormat(t: HtmlTag): String = {
    val prettier = new scala.xml.PrettyPrinter(180, 4)
    prettier.format(scala.xml.XML.loadString(t.toString))
  }

  def method = "method".attr
  def data_dismiss = "data-dismiss".attr
  def data_refresh = "data-refresh".attr
  def data_replace = "data-replace".attr
  def data_replace_closest = "data-replace-closest".attr
  def data_spinner = "data-spinner".attr
  def data_toggle = "data-toggle".attr
  def data_append = "data-append".attr
  def data_refresh_url = "data-refresh-url".attr

  def read_more = "read-more".attr
  def read_less = "read-less".attr

  //def main = div("main".cls)
  def row_fluid     = div("row-fluid".cls)
  def control_group = div("control-group".cls)
  def control_group_inline = div("control-group-inline".cls)
  def control_label = div("control-label".cls)
  def controls      = div("controls".cls)
  def help_block    = div("help-block".cls)
  def help_inline    = div("help-inline".cls)
  def sidebar       = div("sidebar".cls)
  def sidebarbody   = div("sidebarbody".cls)
  def well          = div("well".cls)
  def entireform    = div("entireform".cls)
  def inlineblock   = div("inlineblock".cls)
  def document      = div("document".cls)
  def authors       = div("authors".cls)
  //def rightX      = div("right".cls)


  def image(s:String) = img(src:=s"/assets/$assetsVersion/images/$s")

  //def viewall[T](ts: Seq[T], f: T => HtmlTag) = for {
  //  t <- ts
  //} yield f(t)
  // 
  //def view[T](t: T, f: T=>HtmlTag, attrs: (String, String)*): Modifier= {
  //  f(t)
  //}

}


trait DynamicViews extends CustomHtmlTags {
  import java.lang.reflect.Method

  import scalaz.syntax.id._
  // import custom._
  //import forms._

  def availableViewMethods(): Seq[Method] = {
    val allMethods = this.getClass.getMethods
    val meths = for {
      meth <- allMethods
      ptypes  = meth.getParameterTypes()
      if meth.getReturnType == classOf[Node] && ptypes.lastOption.exists(classOf[Seq[(String, Any)]].isAssignableFrom(_))
    } yield {
      println(s"meth: ${meth.getName()}: ${ptypes.mkString(", ")}")
      meth
    }
    meths
  }

  val availableMethods = availableViewMethods()

  def dynamicView(model: AnyRef, viewName: String, args: Seq[(String, Any)]): Node = {
    println(s"""dynamicView(${model}, ${viewName}, ${args}) """)
    import scala.collection.mutable.ListBuffer

    // build the list of supertypes for our model 
    val viewableClassList = new ListBuffer[Class[_]]()
    def buildViewableClassList(clazz: Class[_]): Unit = {
      if (clazz != null && clazz != classOf[Object] && clazz != classOf[ScalaObject] && !viewableClassList.contains(clazz)) {
        viewableClassList.append(clazz);
        buildViewableClassList(clazz.getSuperclass)
        for (
          i <- clazz.getInterfaces
          if !(i.getName.startsWith("scala.") || i.getName.startsWith("java."))
        ) {
          buildViewableClassList(i)
        }
      }
    }

    buildViewableClassList(model.getClass)

    println(s"""model viewable classes: ${viewableClassList.mkString("\n  ", "\n  ", "\n")}""")

    val possibleMs = (for {
      vt <- viewableClassList
      m <- availableMethods
      if m.getName.startsWith(viewName) && m.getParameterTypes.headOption.exists(_.isAssignableFrom(vt))
    } yield {
      m
    })

    if (possibleMs.isEmpty) {
      println("ERROR: no suitable dynamic view found")
    } else {
      println(s""" possible methods to call (UNSORTED, using first): ${possibleMs.mkString("{\n  ", "\n  ", "\n}")}""")
    }
    //_.invoke(this).asInstanceOf[Seq[(String, Any)] => Node].apply(args).asInstanceOf[Node]
    possibleMs.headOption.map(
      _.invoke(this, model, args).asInstanceOf[Node]
    ).getOrElse(noopNode)
  }
}




// First attempt used scala reflection, but with no success. It's hard.
//  def dynamicView(model: AnyRef, viewName: String, args: Seq[(String, Any)]): Node = {
//    println(s"""dynamicView(${model}, ${viewName}, ${args}) """)
//    import scala.collection.mutable.ListBuffer
// 
//    val runtimeMirror =  ru.runtimeMirror(this.getClass.getClassLoader)
// 
//    // The view-compatible method type we are looking for 
//    val dummyDefType = typeOf[D].member(ru.newTermName("apply"))
//    val dummyDefTypeSig = dummyDefType.typeSignature
// 
// 
//    // build the list of supertypes for our model 
//    val viewableClassList = new ListBuffer[Class[_]]()
//    def viewableClasses(clazz: Class[_]): Unit = {
//      if (clazz != null && clazz != classOf[Object] && clazz != classOf[ScalaObject] && !viewableClassList.contains(clazz)) {
//        viewableClassList.append(clazz);
//        viewableClasses(clazz.getSuperclass)
//        for (
//          i <- clazz.getInterfaces
//          if !(i.getName.startsWith("scala.") || i.getName.startsWith("java."))
//        ) {
//          viewableClasses(i)
//        }
//      }
//    }
// 
//    viewableClasses(model.getClass)
// 
//    // ...and the corresponding scala Type s
//    val viewableTypes: Seq[Type] = viewableClassList.map(getType(_))
// 
//    def viewTypeMethods[T : TypeTag](v: T) = {
//      val vType   = typeOf[T]
//      //vType.members.foreach {
//      //  case m: MethodSymbol if m.name.decoded.endsWith(viewName) =>
//      //    val typesig = m.typeSignatureIn(vType)
//      //    println(s""" trying symbol: ${m}, ${typesig}""")
//      // 
//      //    typesig match {
//      //      case MethodType(argtypes, rtype) if argtypes.size==1 =>
//      //        val argTypes = argtypes.map(_.typeSignature).mkString(",  ")
//      // 
//      //        println(s"""     arg types ${argTypes} """)
//      //        println(s"""     ret type ${rtype} """)
//      //        println(s"""     D.dummy type ${dummyDefType} """)
//      //        println(s"""     D.dummysig type ${dummyDefTypeSig} """)
//      //        println(s"""     want type ${ArgsType} """)
//      //        println(s"""     match args? ${rtype =:= ArgsType} """)
//      //        println(s"""     match rets? ${rtype =:= dummyDefTypeSig} """)
//      //        println(s"""      declarations: ${rtype.declarations.mkString(",  ")} """)
//      //        println(s"""      members:      ${rtype.members.mkString(",  ")} """)
//      //        //println(s"""     typesig.declarations ${typesig.declarations.mkString(",  ")} """)
//      //        //println(s"""     typesig.members       ${typesig.members.mkString(",  ")} """)
//      //    }
//      // 
//      //  case _ =>
//      //}
//      
//      println("All methods in scope")
//      println(vType.members.mkString("{\n  ", "\n  ", "\n}"))
//      println("Endscope")
// 
//      val allTypes = for {
//        base <- vType.baseClasses
//        tpe = base.asType.toType
//        m <- tpe.members.toList
//      } yield {
//        m
//      }
// 
//      println("All methods in *all* scopes")
//      println(allTypes.mkString("{\n  ", "\n  ", "\n}"))
//      println("Endscope")
// 
//      val compatibleMethods = vType.members.collect({
//        case m: MethodSymbol if m.name.decoded.endsWith(viewName) =>
//          println(s"""considering method sym ${m}: ${m.typeSignatureIn(vType)} """)
//          m -> m.typeSignatureIn(vType)
//      }).collect({
//        case (m, mt @ MethodType(argtypes, rtype)) =>
//          val argTypes = argtypes.map(_.typeSignature).mkString(",  ") 
//          println(s"""considering method ${m}: ${argTypes} => ${rtype}""")
//          m -> mt
//      }).collect({
//        case (m, mt @ MethodType(argtypes, rtype)) if rtype =:= dummyDefTypeSig && argtypes.size==1 =>
//          val argTypes = argtypes.map(_.typeSignature).mkString(",  ") 
//          println(s"""choosing compatible method ${m}: ${argTypes} => ${rtype}""")
//          m -> mt
//      })
// 
//      compatibleMethods
//    }
// 
// 
//    val compatibleMethods = viewTypeMethods(this)
// 
//    println(s"""model viewable classes: ${viewableClassList.mkString("\n  ", "  \n", "\n")}""")
//    println(s"""*Views compatible methods: ${compatibleMethods.mkString("\n  ", "  \n", "\n")}""")
// 
// 
//    //viewTypeMethods(this) foreach { case (a, b) =>
//    //  println(s"""found type ${a}: ${b}""")
//    //}
// 
//    val r = (for {
//      vt <- viewableTypes
//      (vsym, MethodType(argTypes, retType)) <- compatibleMethods
//      if vt =:= argTypes.head.typeSignature
//    } yield {
//      println(s"""found type ${vsym.name}: ${argTypes.head.typeSignature}: ${retType}""")
//      val vmeth = vsym.asMethod
//      val thisMirror = runtimeMirror.reflect(this)
//      val vmeth2 = thisMirror.reflectMethod(vmeth)
//      println(s"""vmeth2 = ${vmeth2} """)
//      val asdf = vmeth2(model, args)
//      val result = asdf.asInstanceOf[D].apply(args)
//      println(s"""result = ${result} """)
//      result
//    })
// 
//    //  val mm = im.reflectMethod(viewMethodCandidates.head.asMethod)
//    //  //val mm = im.reflectMethod(viewMethod)
//    //  val ret = mm.apply(model)
//    //  //mm.apply(model)(args)
//    if (r.isEmpty) {
//      println("ERROR: no suitable dynamic view found")
//    }
//    r.headOption.getOrElse(noopNode)
//  }
