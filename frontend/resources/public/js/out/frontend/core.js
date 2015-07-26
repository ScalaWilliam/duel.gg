// Compiled by ClojureScript 0.0-3308 {}
goog.provide('frontend.core');
goog.require('cljs.core');
goog.require('goog.events');
goog.require('cljs.core.async');
goog.require('om.core');
goog.require('om.dom');
goog.require('cljs_http.client');
cljs.core.enable_console_print_BANG_.call(null);
frontend.core.fetch_widgets = (function frontend$core$fetch_widgets(url){
var c = cljs.core.async.chan.call(null);
var c__6614__auto___7548 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6614__auto___7548,c){
return (function (){
var f__6615__auto__ = (function (){var switch__6593__auto__ = ((function (c__6614__auto___7548,c){
return (function (state_7534){
var state_val_7535 = (state_7534[(1)]);
if((state_val_7535 === (1))){
var inst_7520 = cljs_http.client.get.call(null,url);
var state_7534__$1 = state_7534;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_7534__$1,(2),inst_7520);
} else {
if((state_val_7535 === (2))){
var inst_7522 = (state_7534[(7)]);
var inst_7522__$1 = (state_7534[(2)]);
var inst_7523 = cljs.core.seq_QMARK_.call(null,inst_7522__$1);
var state_7534__$1 = (function (){var statearr_7536 = state_7534;
(statearr_7536[(7)] = inst_7522__$1);

return statearr_7536;
})();
if(inst_7523){
var statearr_7537_7549 = state_7534__$1;
(statearr_7537_7549[(1)] = (3));

} else {
var statearr_7538_7550 = state_7534__$1;
(statearr_7538_7550[(1)] = (4));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_7535 === (3))){
var inst_7522 = (state_7534[(7)]);
var inst_7525 = cljs.core.apply.call(null,cljs.core.hash_map,inst_7522);
var state_7534__$1 = state_7534;
var statearr_7539_7551 = state_7534__$1;
(statearr_7539_7551[(2)] = inst_7525);

(statearr_7539_7551[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_7535 === (4))){
var inst_7522 = (state_7534[(7)]);
var state_7534__$1 = state_7534;
var statearr_7540_7552 = state_7534__$1;
(statearr_7540_7552[(2)] = inst_7522);

(statearr_7540_7552[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_7535 === (5))){
var inst_7528 = (state_7534[(2)]);
var inst_7529 = cljs.core.get.call(null,inst_7528,new cljs.core.Keyword(null,"body","body",-2049205669));
var inst_7530 = cljs.core.vec.call(null,inst_7529);
var state_7534__$1 = state_7534;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_7534__$1,(6),c,inst_7530);
} else {
if((state_val_7535 === (6))){
var inst_7532 = (state_7534[(2)]);
var state_7534__$1 = state_7534;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_7534__$1,inst_7532);
} else {
return null;
}
}
}
}
}
}
});})(c__6614__auto___7548,c))
;
return ((function (switch__6593__auto__,c__6614__auto___7548,c){
return (function() {
var frontend$core$fetch_widgets_$_state_machine__6594__auto__ = null;
var frontend$core$fetch_widgets_$_state_machine__6594__auto____0 = (function (){
var statearr_7544 = [null,null,null,null,null,null,null,null];
(statearr_7544[(0)] = frontend$core$fetch_widgets_$_state_machine__6594__auto__);

(statearr_7544[(1)] = (1));

return statearr_7544;
});
var frontend$core$fetch_widgets_$_state_machine__6594__auto____1 = (function (state_7534){
while(true){
var ret_value__6595__auto__ = (function (){try{while(true){
var result__6596__auto__ = switch__6593__auto__.call(null,state_7534);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6596__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6596__auto__;
}
break;
}
}catch (e7545){if((e7545 instanceof Object)){
var ex__6597__auto__ = e7545;
var statearr_7546_7553 = state_7534;
(statearr_7546_7553[(5)] = ex__6597__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_7534);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e7545;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6595__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__7554 = state_7534;
state_7534 = G__7554;
continue;
} else {
return ret_value__6595__auto__;
}
break;
}
});
frontend$core$fetch_widgets_$_state_machine__6594__auto__ = function(state_7534){
switch(arguments.length){
case 0:
return frontend$core$fetch_widgets_$_state_machine__6594__auto____0.call(this);
case 1:
return frontend$core$fetch_widgets_$_state_machine__6594__auto____1.call(this,state_7534);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
frontend$core$fetch_widgets_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$0 = frontend$core$fetch_widgets_$_state_machine__6594__auto____0;
frontend$core$fetch_widgets_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$1 = frontend$core$fetch_widgets_$_state_machine__6594__auto____1;
return frontend$core$fetch_widgets_$_state_machine__6594__auto__;
})()
;})(switch__6593__auto__,c__6614__auto___7548,c))
})();
var state__6616__auto__ = (function (){var statearr_7547 = f__6615__auto__.call(null);
(statearr_7547[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6614__auto___7548);

return statearr_7547;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6616__auto__);
});})(c__6614__auto___7548,c))
);


return c;
});
frontend.core.app_state = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
frontend.core.widget = (function frontend$core$widget(w,owner,opts){
if(typeof frontend.core.t7558 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t7558 = (function (widget,w,owner,opts,meta7559){
this.widget = widget;
this.w = w;
this.owner = owner;
this.opts = opts;
this.meta7559 = meta7559;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t7558.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_7560,meta7559__$1){
var self__ = this;
var _7560__$1 = this;
return (new frontend.core.t7558(self__.widget,self__.w,self__.owner,self__.opts,meta7559__$1));
});

frontend.core.t7558.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_7560){
var self__ = this;
var _7560__$1 = this;
return self__.meta7559;
});

frontend.core.t7558.prototype.om$core$IRender$ = true;

frontend.core.t7558.prototype.om$core$IRender$render$arity$1 = (function (this__7303__auto__){
var self__ = this;
var this__7303__auto____$1 = this;
return React.DOM.li(null,cljs.core.pr_str.call(null,self__.w));
});

frontend.core.t7558.getBasis = (function (){
return new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"widget","widget",786562584,null),new cljs.core.Symbol(null,"w","w",1994700528,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"opts","opts",1795607228,null),new cljs.core.Symbol(null,"meta7559","meta7559",-1967787827,null)], null);
});

frontend.core.t7558.cljs$lang$type = true;

frontend.core.t7558.cljs$lang$ctorStr = "frontend.core/t7558";

frontend.core.t7558.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t7558");
});

frontend.core.__GT_t7558 = (function frontend$core$widget_$___GT_t7558(widget__$1,w__$1,owner__$1,opts__$1,meta7559){
return (new frontend.core.t7558(widget__$1,w__$1,owner__$1,opts__$1,meta7559));
});

}

return (new frontend.core.t7558(frontend$core$widget,w,owner,opts,null));
});
frontend.core.widget_list = (function frontend$core$widget_list(p__7561){
var map__7566 = p__7561;
var map__7566__$1 = ((cljs.core.seq_QMARK_.call(null,map__7566))?cljs.core.apply.call(null,cljs.core.hash_map,map__7566):map__7566);
var widgets = cljs.core.get.call(null,map__7566__$1,new cljs.core.Keyword(null,"widgets","widgets",-159098978));
if(typeof frontend.core.t7567 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t7567 = (function (widget_list,p__7561,map__7566,widgets,meta7568){
this.widget_list = widget_list;
this.p__7561 = p__7561;
this.map__7566 = map__7566;
this.widgets = widgets;
this.meta7568 = meta7568;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t7567.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (map__7566,map__7566__$1,widgets){
return (function (_7569,meta7568__$1){
var self__ = this;
var _7569__$1 = this;
return (new frontend.core.t7567(self__.widget_list,self__.p__7561,self__.map__7566,self__.widgets,meta7568__$1));
});})(map__7566,map__7566__$1,widgets))
;

frontend.core.t7567.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (map__7566,map__7566__$1,widgets){
return (function (_7569){
var self__ = this;
var _7569__$1 = this;
return self__.meta7568;
});})(map__7566,map__7566__$1,widgets))
;

frontend.core.t7567.prototype.om$core$IRender$ = true;

frontend.core.t7567.prototype.om$core$IRender$render$arity$1 = ((function (map__7566,map__7566__$1,widgets){
return (function (this__7303__auto__){
var self__ = this;
var this__7303__auto____$1 = this;
return cljs.core.apply.call(null,om.dom.ul,null,om.core.build_all.call(null,frontend.core.widget,self__.widgets));
});})(map__7566,map__7566__$1,widgets))
;

frontend.core.t7567.getBasis = ((function (map__7566,map__7566__$1,widgets){
return (function (){
return new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"widget-list","widget-list",1944722190,null),new cljs.core.Symbol(null,"p__7561","p__7561",1904839522,null),new cljs.core.Symbol(null,"map__7566","map__7566",1530635884,null),new cljs.core.Symbol(null,"widgets","widgets",1481432549,null),new cljs.core.Symbol(null,"meta7568","meta7568",672028315,null)], null);
});})(map__7566,map__7566__$1,widgets))
;

frontend.core.t7567.cljs$lang$type = true;

frontend.core.t7567.cljs$lang$ctorStr = "frontend.core/t7567";

frontend.core.t7567.cljs$lang$ctorPrWriter = ((function (map__7566,map__7566__$1,widgets){
return (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t7567");
});})(map__7566,map__7566__$1,widgets))
;

frontend.core.__GT_t7567 = ((function (map__7566,map__7566__$1,widgets){
return (function frontend$core$widget_list_$___GT_t7567(widget_list__$1,p__7561__$1,map__7566__$2,widgets__$1,meta7568){
return (new frontend.core.t7567(widget_list__$1,p__7561__$1,map__7566__$2,widgets__$1,meta7568));
});})(map__7566,map__7566__$1,widgets))
;

}

return (new frontend.core.t7567(frontend$core$widget_list,p__7561,map__7566__$1,widgets,null));
});
frontend.core.widget_box = (function frontend$core$widget_box(app,owner,opts){
if(typeof frontend.core.t7612 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t7612 = (function (widget_box,app,owner,opts,meta7613){
this.widget_box = widget_box;
this.app = app;
this.owner = owner;
this.opts = opts;
this.meta7613 = meta7613;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t7612.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_7614,meta7613__$1){
var self__ = this;
var _7614__$1 = this;
return (new frontend.core.t7612(self__.widget_box,self__.app,self__.owner,self__.opts,meta7613__$1));
});

frontend.core.t7612.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_7614){
var self__ = this;
var _7614__$1 = this;
return self__.meta7613;
});

frontend.core.t7612.prototype.om$core$IWillMount$ = true;

frontend.core.t7612.prototype.om$core$IWillMount$will_mount$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
om.core.transact_BANG_.call(null,self__.app,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"widgets","widgets",-159098978)], null),((function (___$1){
return (function (){
return cljs.core.PersistentVector.EMPTY;
});})(___$1))
);

var c__6614__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6614__auto__,___$1){
return (function (){
var f__6615__auto__ = (function (){var switch__6593__auto__ = ((function (c__6614__auto__,___$1){
return (function (state_7637){
var state_val_7638 = (state_7637[(1)]);
if((state_val_7638 === (1))){
var state_7637__$1 = state_7637;
var statearr_7639_7654 = state_7637__$1;
(statearr_7639_7654[(2)] = null);

(statearr_7639_7654[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_7638 === (2))){
var state_7637__$1 = state_7637;
var statearr_7640_7655 = state_7637__$1;
(statearr_7640_7655[(1)] = (4));



return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_7638 === (3))){
var inst_7635 = (state_7637[(2)]);
var state_7637__$1 = state_7637;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_7637__$1,inst_7635);
} else {
if((state_val_7638 === (4))){
var inst_7617 = new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(self__.opts);
var inst_7618 = frontend.core.fetch_widgets.call(null,inst_7617);
var state_7637__$1 = state_7637;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_7637__$1,(7),inst_7618);
} else {
if((state_val_7638 === (5))){
var state_7637__$1 = state_7637;
var statearr_7642_7656 = state_7637__$1;
(statearr_7642_7656[(2)] = null);

(statearr_7642_7656[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_7638 === (6))){
var inst_7633 = (state_7637[(2)]);
var state_7637__$1 = state_7637;
var statearr_7643_7657 = state_7637__$1;
(statearr_7643_7657[(2)] = inst_7633);

(statearr_7643_7657[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_7638 === (7))){
var inst_7620 = (state_7637[(2)]);
var inst_7621 = console.log(inst_7620);
var inst_7622 = cljs.core.PersistentVector.EMPTY_NODE;
var inst_7623 = [new cljs.core.Keyword(null,"widgets","widgets",-159098978)];
var inst_7624 = (new cljs.core.PersistentVector(null,1,(5),inst_7622,inst_7623,null));
var inst_7625 = om.core.update_BANG_.call(null,self__.app,inst_7624,inst_7620);
var inst_7626 = new cljs.core.Keyword(null,"poll-interval","poll-interval",345867570).cljs$core$IFn$_invoke$arity$1(self__.opts);
var inst_7627 = cljs.core.async.timeout.call(null,inst_7626);
var state_7637__$1 = (function (){var statearr_7644 = state_7637;
(statearr_7644[(7)] = inst_7625);

(statearr_7644[(8)] = inst_7621);

return statearr_7644;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_7637__$1,(8),inst_7627);
} else {
if((state_val_7638 === (8))){
var inst_7629 = (state_7637[(2)]);
var state_7637__$1 = (function (){var statearr_7645 = state_7637;
(statearr_7645[(9)] = inst_7629);

return statearr_7645;
})();
var statearr_7646_7658 = state_7637__$1;
(statearr_7646_7658[(2)] = null);

(statearr_7646_7658[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
});})(c__6614__auto__,___$1))
;
return ((function (switch__6593__auto__,c__6614__auto__,___$1){
return (function() {
var frontend$core$widget_box_$_state_machine__6594__auto__ = null;
var frontend$core$widget_box_$_state_machine__6594__auto____0 = (function (){
var statearr_7650 = [null,null,null,null,null,null,null,null,null,null];
(statearr_7650[(0)] = frontend$core$widget_box_$_state_machine__6594__auto__);

(statearr_7650[(1)] = (1));

return statearr_7650;
});
var frontend$core$widget_box_$_state_machine__6594__auto____1 = (function (state_7637){
while(true){
var ret_value__6595__auto__ = (function (){try{while(true){
var result__6596__auto__ = switch__6593__auto__.call(null,state_7637);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6596__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6596__auto__;
}
break;
}
}catch (e7651){if((e7651 instanceof Object)){
var ex__6597__auto__ = e7651;
var statearr_7652_7659 = state_7637;
(statearr_7652_7659[(5)] = ex__6597__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_7637);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e7651;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6595__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__7660 = state_7637;
state_7637 = G__7660;
continue;
} else {
return ret_value__6595__auto__;
}
break;
}
});
frontend$core$widget_box_$_state_machine__6594__auto__ = function(state_7637){
switch(arguments.length){
case 0:
return frontend$core$widget_box_$_state_machine__6594__auto____0.call(this);
case 1:
return frontend$core$widget_box_$_state_machine__6594__auto____1.call(this,state_7637);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
frontend$core$widget_box_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$0 = frontend$core$widget_box_$_state_machine__6594__auto____0;
frontend$core$widget_box_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$1 = frontend$core$widget_box_$_state_machine__6594__auto____1;
return frontend$core$widget_box_$_state_machine__6594__auto__;
})()
;})(switch__6593__auto__,c__6614__auto__,___$1))
})();
var state__6616__auto__ = (function (){var statearr_7653 = f__6615__auto__.call(null);
(statearr_7653[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6614__auto__);

return statearr_7653;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6616__auto__);
});})(c__6614__auto__,___$1))
);

return c__6614__auto__;
});

frontend.core.t7612.prototype.om$core$IRender$ = true;

frontend.core.t7612.prototype.om$core$IRender$render$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return React.DOM.div(null,React.DOM.h1(null,"Widgets"),React.DOM.h2(null,"LELLEL"),om.core.build.call(null,frontend.core.widget_list,self__.app));
});

frontend.core.t7612.getBasis = (function (){
return new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"widget-box","widget-box",1688005306,null),new cljs.core.Symbol(null,"app","app",1079569820,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"opts","opts",1795607228,null),new cljs.core.Symbol(null,"meta7613","meta7613",-728307754,null)], null);
});

frontend.core.t7612.cljs$lang$type = true;

frontend.core.t7612.cljs$lang$ctorStr = "frontend.core/t7612";

frontend.core.t7612.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t7612");
});

frontend.core.__GT_t7612 = (function frontend$core$widget_box_$___GT_t7612(widget_box__$1,app__$1,owner__$1,opts__$1,meta7613){
return (new frontend.core.t7612(widget_box__$1,app__$1,owner__$1,opts__$1,meta7613));
});

}

return (new frontend.core.t7612(frontend$core$widget_box,app,owner,opts,cljs.core.PersistentArrayMap.EMPTY));
});
frontend.core.om_app = (function frontend$core$om_app(app,owner){
if(typeof frontend.core.t7664 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t7664 = (function (om_app,app,owner,meta7665){
this.om_app = om_app;
this.app = app;
this.owner = owner;
this.meta7665 = meta7665;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t7664.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_7666,meta7665__$1){
var self__ = this;
var _7666__$1 = this;
return (new frontend.core.t7664(self__.om_app,self__.app,self__.owner,meta7665__$1));
});

frontend.core.t7664.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_7666){
var self__ = this;
var _7666__$1 = this;
return self__.meta7665;
});

frontend.core.t7664.prototype.om$core$IRender$ = true;

frontend.core.t7664.prototype.om$core$IRender$render$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return React.DOM.div(null,om.core.build.call(null,frontend.core.widget_box,self__.app,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"opts","opts",155075701),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"url","url",276297046),"/duels/",new cljs.core.Keyword(null,"poll-interval","poll-interval",345867570),(2000)], null)], null)));
});

frontend.core.t7664.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"om-app","om-app",-1539149030,null),new cljs.core.Symbol(null,"app","app",1079569820,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"meta7665","meta7665",-1869241249,null)], null);
});

frontend.core.t7664.cljs$lang$type = true;

frontend.core.t7664.cljs$lang$ctorStr = "frontend.core/t7664";

frontend.core.t7664.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t7664");
});

frontend.core.__GT_t7664 = (function frontend$core$om_app_$___GT_t7664(om_app__$1,app__$1,owner__$1,meta7665){
return (new frontend.core.t7664(om_app__$1,app__$1,owner__$1,meta7665));
});

}

return (new frontend.core.t7664(frontend$core$om_app,app,owner,cljs.core.PersistentArrayMap.EMPTY));
});
om.core.root.call(null,frontend.core.om_app,frontend.core.app_state,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"target","target",253001721),document.getElementById("content")], null));

//# sourceMappingURL=core.js.map