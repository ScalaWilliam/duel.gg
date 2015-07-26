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
var c__6614__auto___8092 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6614__auto___8092,c){
return (function (){
var f__6615__auto__ = (function (){var switch__6593__auto__ = ((function (c__6614__auto___8092,c){
return (function (state_8078){
var state_val_8079 = (state_8078[(1)]);
if((state_val_8079 === (1))){
var inst_8064 = cljs_http.client.get.call(null,url);
var state_8078__$1 = state_8078;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_8078__$1,(2),inst_8064);
} else {
if((state_val_8079 === (2))){
var inst_8066 = (state_8078[(7)]);
var inst_8066__$1 = (state_8078[(2)]);
var inst_8067 = cljs.core.seq_QMARK_.call(null,inst_8066__$1);
var state_8078__$1 = (function (){var statearr_8080 = state_8078;
(statearr_8080[(7)] = inst_8066__$1);

return statearr_8080;
})();
if(inst_8067){
var statearr_8081_8093 = state_8078__$1;
(statearr_8081_8093[(1)] = (3));

} else {
var statearr_8082_8094 = state_8078__$1;
(statearr_8082_8094[(1)] = (4));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_8079 === (3))){
var inst_8066 = (state_8078[(7)]);
var inst_8069 = cljs.core.apply.call(null,cljs.core.hash_map,inst_8066);
var state_8078__$1 = state_8078;
var statearr_8083_8095 = state_8078__$1;
(statearr_8083_8095[(2)] = inst_8069);

(statearr_8083_8095[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_8079 === (4))){
var inst_8066 = (state_8078[(7)]);
var state_8078__$1 = state_8078;
var statearr_8084_8096 = state_8078__$1;
(statearr_8084_8096[(2)] = inst_8066);

(statearr_8084_8096[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_8079 === (5))){
var inst_8072 = (state_8078[(2)]);
var inst_8073 = cljs.core.get.call(null,inst_8072,new cljs.core.Keyword(null,"body","body",-2049205669));
var inst_8074 = cljs.core.vec.call(null,inst_8073);
var state_8078__$1 = state_8078;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_8078__$1,(6),c,inst_8074);
} else {
if((state_val_8079 === (6))){
var inst_8076 = (state_8078[(2)]);
var state_8078__$1 = state_8078;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_8078__$1,inst_8076);
} else {
return null;
}
}
}
}
}
}
});})(c__6614__auto___8092,c))
;
return ((function (switch__6593__auto__,c__6614__auto___8092,c){
return (function() {
var frontend$core$fetch_widgets_$_state_machine__6594__auto__ = null;
var frontend$core$fetch_widgets_$_state_machine__6594__auto____0 = (function (){
var statearr_8088 = [null,null,null,null,null,null,null,null];
(statearr_8088[(0)] = frontend$core$fetch_widgets_$_state_machine__6594__auto__);

(statearr_8088[(1)] = (1));

return statearr_8088;
});
var frontend$core$fetch_widgets_$_state_machine__6594__auto____1 = (function (state_8078){
while(true){
var ret_value__6595__auto__ = (function (){try{while(true){
var result__6596__auto__ = switch__6593__auto__.call(null,state_8078);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6596__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6596__auto__;
}
break;
}
}catch (e8089){if((e8089 instanceof Object)){
var ex__6597__auto__ = e8089;
var statearr_8090_8097 = state_8078;
(statearr_8090_8097[(5)] = ex__6597__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_8078);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e8089;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6595__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__8098 = state_8078;
state_8078 = G__8098;
continue;
} else {
return ret_value__6595__auto__;
}
break;
}
});
frontend$core$fetch_widgets_$_state_machine__6594__auto__ = function(state_8078){
switch(arguments.length){
case 0:
return frontend$core$fetch_widgets_$_state_machine__6594__auto____0.call(this);
case 1:
return frontend$core$fetch_widgets_$_state_machine__6594__auto____1.call(this,state_8078);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
frontend$core$fetch_widgets_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$0 = frontend$core$fetch_widgets_$_state_machine__6594__auto____0;
frontend$core$fetch_widgets_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$1 = frontend$core$fetch_widgets_$_state_machine__6594__auto____1;
return frontend$core$fetch_widgets_$_state_machine__6594__auto__;
})()
;})(switch__6593__auto__,c__6614__auto___8092,c))
})();
var state__6616__auto__ = (function (){var statearr_8091 = f__6615__auto__.call(null);
(statearr_8091[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6614__auto___8092);

return statearr_8091;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6616__auto__);
});})(c__6614__auto___8092,c))
);


return c;
});
frontend.core.app_state = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
frontend.core.widget = (function frontend$core$widget(w,owner,opts){
if(typeof frontend.core.t8102 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t8102 = (function (widget,w,owner,opts,meta8103){
this.widget = widget;
this.w = w;
this.owner = owner;
this.opts = opts;
this.meta8103 = meta8103;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t8102.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_8104,meta8103__$1){
var self__ = this;
var _8104__$1 = this;
return (new frontend.core.t8102(self__.widget,self__.w,self__.owner,self__.opts,meta8103__$1));
});

frontend.core.t8102.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_8104){
var self__ = this;
var _8104__$1 = this;
return self__.meta8103;
});

frontend.core.t8102.prototype.om$core$IRender$ = true;

frontend.core.t8102.prototype.om$core$IRender$render$arity$1 = (function (this__7303__auto__){
var self__ = this;
var this__7303__auto____$1 = this;
return React.DOM.li(null,cljs.core.get.call(null,self__.w,new cljs.core.Keyword(null,"simpleId","simpleId",-469392455)));
});

frontend.core.t8102.getBasis = (function (){
return new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"widget","widget",786562584,null),new cljs.core.Symbol(null,"w","w",1994700528,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"opts","opts",1795607228,null),new cljs.core.Symbol(null,"meta8103","meta8103",77174423,null)], null);
});

frontend.core.t8102.cljs$lang$type = true;

frontend.core.t8102.cljs$lang$ctorStr = "frontend.core/t8102";

frontend.core.t8102.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t8102");
});

frontend.core.__GT_t8102 = (function frontend$core$widget_$___GT_t8102(widget__$1,w__$1,owner__$1,opts__$1,meta8103){
return (new frontend.core.t8102(widget__$1,w__$1,owner__$1,opts__$1,meta8103));
});

}

return (new frontend.core.t8102(frontend$core$widget,w,owner,opts,null));
});
frontend.core.widget_list = (function frontend$core$widget_list(p__8105){
var map__8110 = p__8105;
var map__8110__$1 = ((cljs.core.seq_QMARK_.call(null,map__8110))?cljs.core.apply.call(null,cljs.core.hash_map,map__8110):map__8110);
var widgets = cljs.core.get.call(null,map__8110__$1,new cljs.core.Keyword(null,"widgets","widgets",-159098978));
if(typeof frontend.core.t8111 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t8111 = (function (widget_list,p__8105,map__8110,widgets,meta8112){
this.widget_list = widget_list;
this.p__8105 = p__8105;
this.map__8110 = map__8110;
this.widgets = widgets;
this.meta8112 = meta8112;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t8111.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (map__8110,map__8110__$1,widgets){
return (function (_8113,meta8112__$1){
var self__ = this;
var _8113__$1 = this;
return (new frontend.core.t8111(self__.widget_list,self__.p__8105,self__.map__8110,self__.widgets,meta8112__$1));
});})(map__8110,map__8110__$1,widgets))
;

frontend.core.t8111.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (map__8110,map__8110__$1,widgets){
return (function (_8113){
var self__ = this;
var _8113__$1 = this;
return self__.meta8112;
});})(map__8110,map__8110__$1,widgets))
;

frontend.core.t8111.prototype.om$core$IRender$ = true;

frontend.core.t8111.prototype.om$core$IRender$render$arity$1 = ((function (map__8110,map__8110__$1,widgets){
return (function (this__7303__auto__){
var self__ = this;
var this__7303__auto____$1 = this;
return cljs.core.apply.call(null,om.dom.ul,null,om.core.build_all.call(null,frontend.core.widget,self__.widgets));
});})(map__8110,map__8110__$1,widgets))
;

frontend.core.t8111.getBasis = ((function (map__8110,map__8110__$1,widgets){
return (function (){
return new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"widget-list","widget-list",1944722190,null),new cljs.core.Symbol(null,"p__8105","p__8105",1189688335,null),new cljs.core.Symbol(null,"map__8110","map__8110",-10979551,null),new cljs.core.Symbol(null,"widgets","widgets",1481432549,null),new cljs.core.Symbol(null,"meta8112","meta8112",920423762,null)], null);
});})(map__8110,map__8110__$1,widgets))
;

frontend.core.t8111.cljs$lang$type = true;

frontend.core.t8111.cljs$lang$ctorStr = "frontend.core/t8111";

frontend.core.t8111.cljs$lang$ctorPrWriter = ((function (map__8110,map__8110__$1,widgets){
return (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t8111");
});})(map__8110,map__8110__$1,widgets))
;

frontend.core.__GT_t8111 = ((function (map__8110,map__8110__$1,widgets){
return (function frontend$core$widget_list_$___GT_t8111(widget_list__$1,p__8105__$1,map__8110__$2,widgets__$1,meta8112){
return (new frontend.core.t8111(widget_list__$1,p__8105__$1,map__8110__$2,widgets__$1,meta8112));
});})(map__8110,map__8110__$1,widgets))
;

}

return (new frontend.core.t8111(frontend$core$widget_list,p__8105,map__8110__$1,widgets,null));
});
frontend.core.widget_box = (function frontend$core$widget_box(app,owner,opts){
if(typeof frontend.core.t8156 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t8156 = (function (widget_box,app,owner,opts,meta8157){
this.widget_box = widget_box;
this.app = app;
this.owner = owner;
this.opts = opts;
this.meta8157 = meta8157;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t8156.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_8158,meta8157__$1){
var self__ = this;
var _8158__$1 = this;
return (new frontend.core.t8156(self__.widget_box,self__.app,self__.owner,self__.opts,meta8157__$1));
});

frontend.core.t8156.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_8158){
var self__ = this;
var _8158__$1 = this;
return self__.meta8157;
});

frontend.core.t8156.prototype.om$core$IWillMount$ = true;

frontend.core.t8156.prototype.om$core$IWillMount$will_mount$arity$1 = (function (_){
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
return (function (state_8181){
var state_val_8182 = (state_8181[(1)]);
if((state_val_8182 === (1))){
var state_8181__$1 = state_8181;
var statearr_8183_8198 = state_8181__$1;
(statearr_8183_8198[(2)] = null);

(statearr_8183_8198[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_8182 === (2))){
var state_8181__$1 = state_8181;
var statearr_8184_8199 = state_8181__$1;
(statearr_8184_8199[(1)] = (4));



return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_8182 === (3))){
var inst_8179 = (state_8181[(2)]);
var state_8181__$1 = state_8181;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_8181__$1,inst_8179);
} else {
if((state_val_8182 === (4))){
var inst_8161 = new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(self__.opts);
var inst_8162 = frontend.core.fetch_widgets.call(null,inst_8161);
var state_8181__$1 = state_8181;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_8181__$1,(7),inst_8162);
} else {
if((state_val_8182 === (5))){
var state_8181__$1 = state_8181;
var statearr_8186_8200 = state_8181__$1;
(statearr_8186_8200[(2)] = null);

(statearr_8186_8200[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_8182 === (6))){
var inst_8177 = (state_8181[(2)]);
var state_8181__$1 = state_8181;
var statearr_8187_8201 = state_8181__$1;
(statearr_8187_8201[(2)] = inst_8177);

(statearr_8187_8201[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_8182 === (7))){
var inst_8164 = (state_8181[(2)]);
var inst_8165 = console.log(inst_8164);
var inst_8166 = cljs.core.PersistentVector.EMPTY_NODE;
var inst_8167 = [new cljs.core.Keyword(null,"widgets","widgets",-159098978)];
var inst_8168 = (new cljs.core.PersistentVector(null,1,(5),inst_8166,inst_8167,null));
var inst_8169 = om.core.update_BANG_.call(null,self__.app,inst_8168,inst_8164);
var inst_8170 = new cljs.core.Keyword(null,"poll-interval","poll-interval",345867570).cljs$core$IFn$_invoke$arity$1(self__.opts);
var inst_8171 = cljs.core.async.timeout.call(null,inst_8170);
var state_8181__$1 = (function (){var statearr_8188 = state_8181;
(statearr_8188[(7)] = inst_8169);

(statearr_8188[(8)] = inst_8165);

return statearr_8188;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_8181__$1,(8),inst_8171);
} else {
if((state_val_8182 === (8))){
var inst_8173 = (state_8181[(2)]);
var state_8181__$1 = (function (){var statearr_8189 = state_8181;
(statearr_8189[(9)] = inst_8173);

return statearr_8189;
})();
var statearr_8190_8202 = state_8181__$1;
(statearr_8190_8202[(2)] = null);

(statearr_8190_8202[(1)] = (2));


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
var statearr_8194 = [null,null,null,null,null,null,null,null,null,null];
(statearr_8194[(0)] = frontend$core$widget_box_$_state_machine__6594__auto__);

(statearr_8194[(1)] = (1));

return statearr_8194;
});
var frontend$core$widget_box_$_state_machine__6594__auto____1 = (function (state_8181){
while(true){
var ret_value__6595__auto__ = (function (){try{while(true){
var result__6596__auto__ = switch__6593__auto__.call(null,state_8181);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6596__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6596__auto__;
}
break;
}
}catch (e8195){if((e8195 instanceof Object)){
var ex__6597__auto__ = e8195;
var statearr_8196_8203 = state_8181;
(statearr_8196_8203[(5)] = ex__6597__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_8181);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e8195;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6595__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__8204 = state_8181;
state_8181 = G__8204;
continue;
} else {
return ret_value__6595__auto__;
}
break;
}
});
frontend$core$widget_box_$_state_machine__6594__auto__ = function(state_8181){
switch(arguments.length){
case 0:
return frontend$core$widget_box_$_state_machine__6594__auto____0.call(this);
case 1:
return frontend$core$widget_box_$_state_machine__6594__auto____1.call(this,state_8181);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
frontend$core$widget_box_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$0 = frontend$core$widget_box_$_state_machine__6594__auto____0;
frontend$core$widget_box_$_state_machine__6594__auto__.cljs$core$IFn$_invoke$arity$1 = frontend$core$widget_box_$_state_machine__6594__auto____1;
return frontend$core$widget_box_$_state_machine__6594__auto__;
})()
;})(switch__6593__auto__,c__6614__auto__,___$1))
})();
var state__6616__auto__ = (function (){var statearr_8197 = f__6615__auto__.call(null);
(statearr_8197[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6614__auto__);

return statearr_8197;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6616__auto__);
});})(c__6614__auto__,___$1))
);

return c__6614__auto__;
});

frontend.core.t8156.prototype.om$core$IRender$ = true;

frontend.core.t8156.prototype.om$core$IRender$render$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return React.DOM.div(null,React.DOM.h1(null,"Widgets"),React.DOM.h2(null,"LELLEL"),om.core.build.call(null,frontend.core.widget_list,self__.app));
});

frontend.core.t8156.getBasis = (function (){
return new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"widget-box","widget-box",1688005306,null),new cljs.core.Symbol(null,"app","app",1079569820,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"opts","opts",1795607228,null),new cljs.core.Symbol(null,"meta8157","meta8157",-907404444,null)], null);
});

frontend.core.t8156.cljs$lang$type = true;

frontend.core.t8156.cljs$lang$ctorStr = "frontend.core/t8156";

frontend.core.t8156.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t8156");
});

frontend.core.__GT_t8156 = (function frontend$core$widget_box_$___GT_t8156(widget_box__$1,app__$1,owner__$1,opts__$1,meta8157){
return (new frontend.core.t8156(widget_box__$1,app__$1,owner__$1,opts__$1,meta8157));
});

}

return (new frontend.core.t8156(frontend$core$widget_box,app,owner,opts,cljs.core.PersistentArrayMap.EMPTY));
});
frontend.core.om_app = (function frontend$core$om_app(app,owner){
if(typeof frontend.core.t8208 !== 'undefined'){
} else {

/**
* @constructor
*/
frontend.core.t8208 = (function (om_app,app,owner,meta8209){
this.om_app = om_app;
this.app = app;
this.owner = owner;
this.meta8209 = meta8209;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
frontend.core.t8208.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_8210,meta8209__$1){
var self__ = this;
var _8210__$1 = this;
return (new frontend.core.t8208(self__.om_app,self__.app,self__.owner,meta8209__$1));
});

frontend.core.t8208.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_8210){
var self__ = this;
var _8210__$1 = this;
return self__.meta8209;
});

frontend.core.t8208.prototype.om$core$IRender$ = true;

frontend.core.t8208.prototype.om$core$IRender$render$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return React.DOM.div(null,om.core.build.call(null,frontend.core.widget_box,self__.app,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"opts","opts",155075701),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"url","url",276297046),"/duels",new cljs.core.Keyword(null,"poll-interval","poll-interval",345867570),(2000)], null)], null)));
});

frontend.core.t8208.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"om-app","om-app",-1539149030,null),new cljs.core.Symbol(null,"app","app",1079569820,null),new cljs.core.Symbol(null,"owner","owner",1247919588,null),new cljs.core.Symbol(null,"meta8209","meta8209",-34026836,null)], null);
});

frontend.core.t8208.cljs$lang$type = true;

frontend.core.t8208.cljs$lang$ctorStr = "frontend.core/t8208";

frontend.core.t8208.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"frontend.core/t8208");
});

frontend.core.__GT_t8208 = (function frontend$core$om_app_$___GT_t8208(om_app__$1,app__$1,owner__$1,meta8209){
return (new frontend.core.t8208(om_app__$1,app__$1,owner__$1,meta8209));
});

}

return (new frontend.core.t8208(frontend$core$om_app,app,owner,cljs.core.PersistentArrayMap.EMPTY));
});
om.core.root.call(null,frontend.core.om_app,frontend.core.app_state,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"target","target",253001721),document.getElementById("content")], null));

//# sourceMappingURL=core.js.map