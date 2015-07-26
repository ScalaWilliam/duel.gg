// Compiled by ClojureScript 0.0-3308 {}
goog.provide('cljs_http.core');
goog.require('cljs.core');
goog.require('goog.net.ErrorCode');
goog.require('goog.net.EventType');
goog.require('cljs.core.async');
goog.require('cljs_http.util');
goog.require('goog.net.Jsonp');
goog.require('clojure.string');
goog.require('goog.net.XhrIo');
cljs_http.core.pending_requests = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
/**
 * Attempt to close the given channel and abort the pending HTTP request
 * with which it is associated.
 */
cljs_http.core.abort_BANG_ = (function cljs_http$core$abort_BANG_(channel){
var temp__4425__auto__ = cljs.core.deref.call(null,cljs_http.core.pending_requests).call(null,channel);
if(cljs.core.truth_(temp__4425__auto__)){
var req = temp__4425__auto__;
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

cljs.core.async.close_BANG_.call(null,channel);

if(cljs.core.truth_(req.hasOwnProperty("abort"))){
return req.abort();
} else {
return new cljs.core.Keyword(null,"jsonp","jsonp",226119588).cljs$core$IFn$_invoke$arity$1(req).cancel(new cljs.core.Keyword(null,"request","request",1772954723).cljs$core$IFn$_invoke$arity$1(req));
}
} else {
return null;
}
});
cljs_http.core.aborted_QMARK_ = (function cljs_http$core$aborted_QMARK_(xhr){
return cljs.core._EQ_.call(null,xhr.getLastErrorCode(),goog.net.ErrorCode.ABORT);
});
/**
 * Takes an XhrIo object and applies the default-headers to it.
 */
cljs_http.core.apply_default_headers_BANG_ = (function cljs_http$core$apply_default_headers_BANG_(xhr,headers){
var seq__11851 = cljs.core.seq.call(null,cljs.core.map.call(null,cljs_http.util.camelize,cljs.core.keys.call(null,headers)));
var chunk__11856 = null;
var count__11857 = (0);
var i__11858 = (0);
while(true){
if((i__11858 < count__11857)){
var h_name = cljs.core._nth.call(null,chunk__11856,i__11858);
var seq__11859_11863 = cljs.core.seq.call(null,cljs.core.vals.call(null,headers));
var chunk__11860_11864 = null;
var count__11861_11865 = (0);
var i__11862_11866 = (0);
while(true){
if((i__11862_11866 < count__11861_11865)){
var h_val_11867 = cljs.core._nth.call(null,chunk__11860_11864,i__11862_11866);
xhr.headers.set(h_name,h_val_11867);

var G__11868 = seq__11859_11863;
var G__11869 = chunk__11860_11864;
var G__11870 = count__11861_11865;
var G__11871 = (i__11862_11866 + (1));
seq__11859_11863 = G__11868;
chunk__11860_11864 = G__11869;
count__11861_11865 = G__11870;
i__11862_11866 = G__11871;
continue;
} else {
var temp__4425__auto___11872 = cljs.core.seq.call(null,seq__11859_11863);
if(temp__4425__auto___11872){
var seq__11859_11873__$1 = temp__4425__auto___11872;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__11859_11873__$1)){
var c__5106__auto___11874 = cljs.core.chunk_first.call(null,seq__11859_11873__$1);
var G__11875 = cljs.core.chunk_rest.call(null,seq__11859_11873__$1);
var G__11876 = c__5106__auto___11874;
var G__11877 = cljs.core.count.call(null,c__5106__auto___11874);
var G__11878 = (0);
seq__11859_11863 = G__11875;
chunk__11860_11864 = G__11876;
count__11861_11865 = G__11877;
i__11862_11866 = G__11878;
continue;
} else {
var h_val_11879 = cljs.core.first.call(null,seq__11859_11873__$1);
xhr.headers.set(h_name,h_val_11879);

var G__11880 = cljs.core.next.call(null,seq__11859_11873__$1);
var G__11881 = null;
var G__11882 = (0);
var G__11883 = (0);
seq__11859_11863 = G__11880;
chunk__11860_11864 = G__11881;
count__11861_11865 = G__11882;
i__11862_11866 = G__11883;
continue;
}
} else {
}
}
break;
}

var G__11884 = seq__11851;
var G__11885 = chunk__11856;
var G__11886 = count__11857;
var G__11887 = (i__11858 + (1));
seq__11851 = G__11884;
chunk__11856 = G__11885;
count__11857 = G__11886;
i__11858 = G__11887;
continue;
} else {
var temp__4425__auto__ = cljs.core.seq.call(null,seq__11851);
if(temp__4425__auto__){
var seq__11851__$1 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__11851__$1)){
var c__5106__auto__ = cljs.core.chunk_first.call(null,seq__11851__$1);
var G__11888 = cljs.core.chunk_rest.call(null,seq__11851__$1);
var G__11889 = c__5106__auto__;
var G__11890 = cljs.core.count.call(null,c__5106__auto__);
var G__11891 = (0);
seq__11851 = G__11888;
chunk__11856 = G__11889;
count__11857 = G__11890;
i__11858 = G__11891;
continue;
} else {
var h_name = cljs.core.first.call(null,seq__11851__$1);
var seq__11852_11892 = cljs.core.seq.call(null,cljs.core.vals.call(null,headers));
var chunk__11853_11893 = null;
var count__11854_11894 = (0);
var i__11855_11895 = (0);
while(true){
if((i__11855_11895 < count__11854_11894)){
var h_val_11896 = cljs.core._nth.call(null,chunk__11853_11893,i__11855_11895);
xhr.headers.set(h_name,h_val_11896);

var G__11897 = seq__11852_11892;
var G__11898 = chunk__11853_11893;
var G__11899 = count__11854_11894;
var G__11900 = (i__11855_11895 + (1));
seq__11852_11892 = G__11897;
chunk__11853_11893 = G__11898;
count__11854_11894 = G__11899;
i__11855_11895 = G__11900;
continue;
} else {
var temp__4425__auto___11901__$1 = cljs.core.seq.call(null,seq__11852_11892);
if(temp__4425__auto___11901__$1){
var seq__11852_11902__$1 = temp__4425__auto___11901__$1;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__11852_11902__$1)){
var c__5106__auto___11903 = cljs.core.chunk_first.call(null,seq__11852_11902__$1);
var G__11904 = cljs.core.chunk_rest.call(null,seq__11852_11902__$1);
var G__11905 = c__5106__auto___11903;
var G__11906 = cljs.core.count.call(null,c__5106__auto___11903);
var G__11907 = (0);
seq__11852_11892 = G__11904;
chunk__11853_11893 = G__11905;
count__11854_11894 = G__11906;
i__11855_11895 = G__11907;
continue;
} else {
var h_val_11908 = cljs.core.first.call(null,seq__11852_11902__$1);
xhr.headers.set(h_name,h_val_11908);

var G__11909 = cljs.core.next.call(null,seq__11852_11902__$1);
var G__11910 = null;
var G__11911 = (0);
var G__11912 = (0);
seq__11852_11892 = G__11909;
chunk__11853_11893 = G__11910;
count__11854_11894 = G__11911;
i__11855_11895 = G__11912;
continue;
}
} else {
}
}
break;
}

var G__11913 = cljs.core.next.call(null,seq__11851__$1);
var G__11914 = null;
var G__11915 = (0);
var G__11916 = (0);
seq__11851 = G__11913;
chunk__11856 = G__11914;
count__11857 = G__11915;
i__11858 = G__11916;
continue;
}
} else {
return null;
}
}
break;
}
});
/**
 * Takes an XhrIo object and sets response-type if not nil.
 */
cljs_http.core.apply_response_type_BANG_ = (function cljs_http$core$apply_response_type_BANG_(xhr,response_type){
return xhr.setResponseType((function (){var G__11918 = response_type;
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"array-buffer","array-buffer",519008380),G__11918)){
return goog.net.XhrIo.ResponseType.ARRAY_BUFFER;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"blob","blob",1636965233),G__11918)){
return goog.net.XhrIo.ResponseType.BLOB;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"document","document",-1329188687),G__11918)){
return goog.net.XhrIo.ResponseType.DOCUMENT;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"text","text",-1790561697),G__11918)){
return goog.net.XhrIo.ResponseType.TEXT;
} else {
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"default","default",-1987822328),G__11918)){
return goog.net.XhrIo.ResponseType.DEFAULT;
} else {
if(cljs.core._EQ_.call(null,null,G__11918)){
return goog.net.XhrIo.ResponseType.DEFAULT;
} else {
throw (new Error([cljs.core.str("No matching clause: "),cljs.core.str(response_type)].join('')));

}
}
}
}
}
}
})());
});
/**
 * Builds an XhrIo object from the request parameters.
 */
cljs_http.core.build_xhr = (function cljs_http$core$build_xhr(p__11919){
var map__11922 = p__11919;
var map__11922__$1 = ((cljs.core.seq_QMARK_.call(null,map__11922))?cljs.core.apply.call(null,cljs.core.hash_map,map__11922):map__11922);
var request = map__11922__$1;
var with_credentials_QMARK_ = cljs.core.get.call(null,map__11922__$1,new cljs.core.Keyword(null,"with-credentials?","with-credentials?",-1773202222));
var default_headers = cljs.core.get.call(null,map__11922__$1,new cljs.core.Keyword(null,"default-headers","default-headers",-43146094));
var response_type = cljs.core.get.call(null,map__11922__$1,new cljs.core.Keyword(null,"response-type","response-type",-1493770458));
var timeout = (function (){var or__4321__auto__ = new cljs.core.Keyword(null,"timeout","timeout",-318625318).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return (0);
}
})();
var send_credentials = (((with_credentials_QMARK_ == null))?true:with_credentials_QMARK_);
var G__11923 = (new goog.net.XhrIo());
cljs_http.core.apply_default_headers_BANG_.call(null,G__11923,default_headers);

cljs_http.core.apply_response_type_BANG_.call(null,G__11923,response_type);

G__11923.setTimeoutInterval(timeout);

G__11923.setWithCredentials(send_credentials);

return G__11923;
});
cljs_http.core.error_kw = cljs.core.PersistentHashMap.fromArrays([(0),(7),(1),(4),(6),(3),(2),(9),(5),(8)],[new cljs.core.Keyword(null,"no-error","no-error",1984610064),new cljs.core.Keyword(null,"abort","abort",521193198),new cljs.core.Keyword(null,"access-denied","access-denied",959449406),new cljs.core.Keyword(null,"custom-error","custom-error",-1565161123),new cljs.core.Keyword(null,"http-error","http-error",-1040049553),new cljs.core.Keyword(null,"ff-silent-error","ff-silent-error",189390514),new cljs.core.Keyword(null,"file-not-found","file-not-found",-65398940),new cljs.core.Keyword(null,"offline","offline",-107631935),new cljs.core.Keyword(null,"exception","exception",-335277064),new cljs.core.Keyword(null,"timeout","timeout",-318625318)]);
/**
 * Execute the HTTP request corresponding to the given Ring request
 * map and return a core.async channel.
 */
cljs_http.core.xhr = (function cljs_http$core$xhr(p__11924){
var map__11950 = p__11924;
var map__11950__$1 = ((cljs.core.seq_QMARK_.call(null,map__11950))?cljs.core.apply.call(null,cljs.core.hash_map,map__11950):map__11950);
var request = map__11950__$1;
var request_method = cljs.core.get.call(null,map__11950__$1,new cljs.core.Keyword(null,"request-method","request-method",1764796830));
var headers = cljs.core.get.call(null,map__11950__$1,new cljs.core.Keyword(null,"headers","headers",-835030129));
var body = cljs.core.get.call(null,map__11950__$1,new cljs.core.Keyword(null,"body","body",-2049205669));
var with_credentials_QMARK_ = cljs.core.get.call(null,map__11950__$1,new cljs.core.Keyword(null,"with-credentials?","with-credentials?",-1773202222));
var cancel = cljs.core.get.call(null,map__11950__$1,new cljs.core.Keyword(null,"cancel","cancel",-1964088360));
var channel = cljs.core.async.chan.call(null);
var request_url = cljs_http.util.build_url.call(null,request);
var method = cljs.core.name.call(null,(function (){var or__4321__auto__ = request_method;
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return new cljs.core.Keyword(null,"get","get",1683182755);
}
})());
var headers__$1 = cljs_http.util.build_headers.call(null,headers);
var xhr__$1 = cljs_http.core.build_xhr.call(null,request);
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.assoc,channel,xhr__$1);

xhr__$1.listen(goog.net.EventType.COMPLETE,((function (channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function (evt){
var target = evt.target;
var response = new cljs.core.PersistentArrayMap(null, 7, [new cljs.core.Keyword(null,"status","status",-1997798413),target.getStatus(),new cljs.core.Keyword(null,"success","success",1890645906),target.isSuccess(),new cljs.core.Keyword(null,"body","body",-2049205669),target.getResponse(),new cljs.core.Keyword(null,"headers","headers",-835030129),cljs_http.util.parse_headers.call(null,target.getAllResponseHeaders()),new cljs.core.Keyword(null,"trace-redirects","trace-redirects",-1149427907),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [request_url,target.getLastUri()], null),new cljs.core.Keyword(null,"error-code","error-code",180497232),cljs_http.core.error_kw.call(null,target.getLastErrorCode()),new cljs.core.Keyword(null,"error-text","error-text",2021893718),target.getLastError()], null);
if(cljs.core.not.call(null,cljs_http.core.aborted_QMARK_.call(null,xhr__$1))){
cljs.core.async.put_BANG_.call(null,channel,response);
} else {
}

cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

if(cljs.core.truth_(cancel)){
cljs.core.async.close_BANG_.call(null,cancel);
} else {
}

return cljs.core.async.close_BANG_.call(null,channel);
});})(channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
);

xhr__$1.send(request_url,method,body,headers__$1);

if(cljs.core.truth_(cancel)){
var c__6803__auto___11975 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___11975,channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___11975,channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function (state_11961){
var state_val_11962 = (state_11961[(1)]);
if((state_val_11962 === (1))){
var state_11961__$1 = state_11961;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_11961__$1,(2),cancel);
} else {
if((state_val_11962 === (2))){
var inst_11952 = (state_11961[(2)]);
var inst_11953 = xhr__$1.isComplete();
var inst_11954 = cljs.core.not.call(null,inst_11953);
var state_11961__$1 = (function (){var statearr_11963 = state_11961;
(statearr_11963[(7)] = inst_11952);

return statearr_11963;
})();
if(inst_11954){
var statearr_11964_11976 = state_11961__$1;
(statearr_11964_11976[(1)] = (3));

} else {
var statearr_11965_11977 = state_11961__$1;
(statearr_11965_11977[(1)] = (4));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_11962 === (3))){
var inst_11956 = xhr__$1.abort();
var state_11961__$1 = state_11961;
var statearr_11966_11978 = state_11961__$1;
(statearr_11966_11978[(2)] = inst_11956);

(statearr_11966_11978[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_11962 === (4))){
var state_11961__$1 = state_11961;
var statearr_11967_11979 = state_11961__$1;
(statearr_11967_11979[(2)] = null);

(statearr_11967_11979[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_11962 === (5))){
var inst_11959 = (state_11961[(2)]);
var state_11961__$1 = state_11961;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_11961__$1,inst_11959);
} else {
return null;
}
}
}
}
}
});})(c__6803__auto___11975,channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
;
return ((function (switch__6741__auto__,c__6803__auto___11975,channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel){
return (function() {
var cljs_http$core$xhr_$_state_machine__6742__auto__ = null;
var cljs_http$core$xhr_$_state_machine__6742__auto____0 = (function (){
var statearr_11971 = [null,null,null,null,null,null,null,null];
(statearr_11971[(0)] = cljs_http$core$xhr_$_state_machine__6742__auto__);

(statearr_11971[(1)] = (1));

return statearr_11971;
});
var cljs_http$core$xhr_$_state_machine__6742__auto____1 = (function (state_11961){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_11961);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e11972){if((e11972 instanceof Object)){
var ex__6745__auto__ = e11972;
var statearr_11973_11980 = state_11961;
(statearr_11973_11980[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_11961);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e11972;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__11981 = state_11961;
state_11961 = G__11981;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs_http$core$xhr_$_state_machine__6742__auto__ = function(state_11961){
switch(arguments.length){
case 0:
return cljs_http$core$xhr_$_state_machine__6742__auto____0.call(this);
case 1:
return cljs_http$core$xhr_$_state_machine__6742__auto____1.call(this,state_11961);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs_http$core$xhr_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs_http$core$xhr_$_state_machine__6742__auto____0;
cljs_http$core$xhr_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs_http$core$xhr_$_state_machine__6742__auto____1;
return cljs_http$core$xhr_$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___11975,channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
})();
var state__6805__auto__ = (function (){var statearr_11974 = f__6804__auto__.call(null);
(statearr_11974[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___11975);

return statearr_11974;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___11975,channel,request_url,method,headers__$1,xhr__$1,map__11950,map__11950__$1,request,request_method,headers,body,with_credentials_QMARK_,cancel))
);

} else {
}

return channel;
});
/**
 * Execute the JSONP request corresponding to the given Ring request
 * map and return a core.async channel.
 */
cljs_http.core.jsonp = (function cljs_http$core$jsonp(p__11982){
var map__11998 = p__11982;
var map__11998__$1 = ((cljs.core.seq_QMARK_.call(null,map__11998))?cljs.core.apply.call(null,cljs.core.hash_map,map__11998):map__11998);
var request = map__11998__$1;
var timeout = cljs.core.get.call(null,map__11998__$1,new cljs.core.Keyword(null,"timeout","timeout",-318625318));
var callback_name = cljs.core.get.call(null,map__11998__$1,new cljs.core.Keyword(null,"callback-name","callback-name",336964714));
var cancel = cljs.core.get.call(null,map__11998__$1,new cljs.core.Keyword(null,"cancel","cancel",-1964088360));
var channel = cljs.core.async.chan.call(null);
var jsonp__$1 = (new goog.net.Jsonp(cljs_http.util.build_url.call(null,request),callback_name));
jsonp__$1.setRequestTimeout(timeout);

var req_12013 = jsonp__$1.send(null,((function (channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel){
return (function cljs_http$core$jsonp_$_success_callback(data){
var response = new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"status","status",-1997798413),(200),new cljs.core.Keyword(null,"success","success",1890645906),true,new cljs.core.Keyword(null,"body","body",-2049205669),cljs.core.js__GT_clj.call(null,data,new cljs.core.Keyword(null,"keywordize-keys","keywordize-keys",1310784252),true)], null);
cljs.core.async.put_BANG_.call(null,channel,response);

cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

if(cljs.core.truth_(cancel)){
cljs.core.async.close_BANG_.call(null,cancel);
} else {
}

return cljs.core.async.close_BANG_.call(null,channel);
});})(channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel))
,((function (channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel){
return (function cljs_http$core$jsonp_$_error_callback(){
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.dissoc,channel);

if(cljs.core.truth_(cancel)){
cljs.core.async.close_BANG_.call(null,cancel);
} else {
}

return cljs.core.async.close_BANG_.call(null,channel);
});})(channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel))
);
cljs.core.swap_BANG_.call(null,cljs_http.core.pending_requests,cljs.core.assoc,channel,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"jsonp","jsonp",226119588),jsonp__$1,new cljs.core.Keyword(null,"request","request",1772954723),req_12013], null));

if(cljs.core.truth_(cancel)){
var c__6803__auto___12014 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___12014,req_12013,channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___12014,req_12013,channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel){
return (function (state_12003){
var state_val_12004 = (state_12003[(1)]);
if((state_val_12004 === (1))){
var state_12003__$1 = state_12003;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12003__$1,(2),cancel);
} else {
if((state_val_12004 === (2))){
var inst_12000 = (state_12003[(2)]);
var inst_12001 = jsonp__$1.cancel(req_12013);
var state_12003__$1 = (function (){var statearr_12005 = state_12003;
(statearr_12005[(7)] = inst_12000);

return statearr_12005;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12003__$1,inst_12001);
} else {
return null;
}
}
});})(c__6803__auto___12014,req_12013,channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel))
;
return ((function (switch__6741__auto__,c__6803__auto___12014,req_12013,channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel){
return (function() {
var cljs_http$core$jsonp_$_state_machine__6742__auto__ = null;
var cljs_http$core$jsonp_$_state_machine__6742__auto____0 = (function (){
var statearr_12009 = [null,null,null,null,null,null,null,null];
(statearr_12009[(0)] = cljs_http$core$jsonp_$_state_machine__6742__auto__);

(statearr_12009[(1)] = (1));

return statearr_12009;
});
var cljs_http$core$jsonp_$_state_machine__6742__auto____1 = (function (state_12003){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12003);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12010){if((e12010 instanceof Object)){
var ex__6745__auto__ = e12010;
var statearr_12011_12015 = state_12003;
(statearr_12011_12015[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12003);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12010;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12016 = state_12003;
state_12003 = G__12016;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs_http$core$jsonp_$_state_machine__6742__auto__ = function(state_12003){
switch(arguments.length){
case 0:
return cljs_http$core$jsonp_$_state_machine__6742__auto____0.call(this);
case 1:
return cljs_http$core$jsonp_$_state_machine__6742__auto____1.call(this,state_12003);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs_http$core$jsonp_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs_http$core$jsonp_$_state_machine__6742__auto____0;
cljs_http$core$jsonp_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs_http$core$jsonp_$_state_machine__6742__auto____1;
return cljs_http$core$jsonp_$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___12014,req_12013,channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel))
})();
var state__6805__auto__ = (function (){var statearr_12012 = f__6804__auto__.call(null);
(statearr_12012[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___12014);

return statearr_12012;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___12014,req_12013,channel,jsonp__$1,map__11998,map__11998__$1,request,timeout,callback_name,cancel))
);

} else {
}

return channel;
});
/**
 * Execute the HTTP request corresponding to the given Ring request
 * map and return a core.async channel.
 */
cljs_http.core.request = (function cljs_http$core$request(p__12017){
var map__12019 = p__12017;
var map__12019__$1 = ((cljs.core.seq_QMARK_.call(null,map__12019))?cljs.core.apply.call(null,cljs.core.hash_map,map__12019):map__12019);
var request__$1 = map__12019__$1;
var request_method = cljs.core.get.call(null,map__12019__$1,new cljs.core.Keyword(null,"request-method","request-method",1764796830));
if(cljs.core._EQ_.call(null,request_method,new cljs.core.Keyword(null,"jsonp","jsonp",226119588))){
return cljs_http.core.jsonp.call(null,request__$1);
} else {
return cljs_http.core.xhr.call(null,request__$1);
}
});

//# sourceMappingURL=core.js.map