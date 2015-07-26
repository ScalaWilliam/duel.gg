// Compiled by ClojureScript 0.0-3308 {}
goog.provide('cljs.core.async');
goog.require('cljs.core');
goog.require('cljs.core.async.impl.channels');
goog.require('cljs.core.async.impl.dispatch');
goog.require('cljs.core.async.impl.ioc_helpers');
goog.require('cljs.core.async.impl.protocols');
goog.require('cljs.core.async.impl.buffers');
goog.require('cljs.core.async.impl.timers');
cljs.core.async.fn_handler = (function cljs$core$async$fn_handler(f){
if(typeof cljs.core.async.t12025 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t12025 = (function (fn_handler,f,meta12026){
this.fn_handler = fn_handler;
this.f = f;
this.meta12026 = meta12026;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t12025.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_12027,meta12026__$1){
var self__ = this;
var _12027__$1 = this;
return (new cljs.core.async.t12025(self__.fn_handler,self__.f,meta12026__$1));
});

cljs.core.async.t12025.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_12027){
var self__ = this;
var _12027__$1 = this;
return self__.meta12026;
});

cljs.core.async.t12025.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t12025.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return true;
});

cljs.core.async.t12025.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__.f;
});

cljs.core.async.t12025.getBasis = (function (){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"fn-handler","fn-handler",648785851,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"meta12026","meta12026",1929909112,null)], null);
});

cljs.core.async.t12025.cljs$lang$type = true;

cljs.core.async.t12025.cljs$lang$ctorStr = "cljs.core.async/t12025";

cljs.core.async.t12025.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t12025");
});

cljs.core.async.__GT_t12025 = (function cljs$core$async$fn_handler_$___GT_t12025(fn_handler__$1,f__$1,meta12026){
return (new cljs.core.async.t12025(fn_handler__$1,f__$1,meta12026));
});

}

return (new cljs.core.async.t12025(cljs$core$async$fn_handler,f,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Returns a fixed buffer of size n. When full, puts will block/park.
 */
cljs.core.async.buffer = (function cljs$core$async$buffer(n){
return cljs.core.async.impl.buffers.fixed_buffer.call(null,n);
});
/**
 * Returns a buffer of size n. When full, puts will complete but
 * val will be dropped (no transfer).
 */
cljs.core.async.dropping_buffer = (function cljs$core$async$dropping_buffer(n){
return cljs.core.async.impl.buffers.dropping_buffer.call(null,n);
});
/**
 * Returns a buffer of size n. When full, puts will complete, and be
 * buffered, but oldest elements in buffer will be dropped (not
 * transferred).
 */
cljs.core.async.sliding_buffer = (function cljs$core$async$sliding_buffer(n){
return cljs.core.async.impl.buffers.sliding_buffer.call(null,n);
});
/**
 * Returns true if a channel created with buff will never block. That is to say,
 * puts into this buffer will never cause the buffer to be full.
 */
cljs.core.async.unblocking_buffer_QMARK_ = (function cljs$core$async$unblocking_buffer_QMARK_(buff){
var G__12029 = buff;
if(G__12029){
var bit__4995__auto__ = null;
if(cljs.core.truth_((function (){var or__4321__auto__ = bit__4995__auto__;
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return G__12029.cljs$core$async$impl$protocols$UnblockingBuffer$;
}
})())){
return true;
} else {
if((!G__12029.cljs$lang$protocol_mask$partition$)){
return cljs.core.native_satisfies_QMARK_.call(null,cljs.core.async.impl.protocols.UnblockingBuffer,G__12029);
} else {
return false;
}
}
} else {
return cljs.core.native_satisfies_QMARK_.call(null,cljs.core.async.impl.protocols.UnblockingBuffer,G__12029);
}
});
/**
 * Creates a channel with an optional buffer, an optional transducer (like (map f),
 * (filter p) etc or a composition thereof), and an optional exception handler.
 * If buf-or-n is a number, will create and use a fixed buffer of that size. If a
 * transducer is supplied a buffer must be specified. ex-handler must be a
 * fn of one argument - if an exception occurs during transformation it will be called
 * with the thrown value as an argument, and any non-nil return value will be placed
 * in the channel.
 */
cljs.core.async.chan = (function cljs$core$async$chan(){
var G__12031 = arguments.length;
switch (G__12031) {
case 0:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$0 = (function (){
return cljs.core.async.chan.call(null,null);
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$1 = (function (buf_or_n){
return cljs.core.async.chan.call(null,buf_or_n,null,null);
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$2 = (function (buf_or_n,xform){
return cljs.core.async.chan.call(null,buf_or_n,xform,null);
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$3 = (function (buf_or_n,xform,ex_handler){
var buf_or_n__$1 = ((cljs.core._EQ_.call(null,buf_or_n,(0)))?null:buf_or_n);
if(cljs.core.truth_(xform)){
if(cljs.core.truth_(buf_or_n__$1)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str("buffer must be supplied when transducer is"),cljs.core.str("\n"),cljs.core.str(cljs.core.pr_str.call(null,new cljs.core.Symbol(null,"buf-or-n","buf-or-n",-1646815050,null)))].join('')));
}
} else {
}

return cljs.core.async.impl.channels.chan.call(null,((typeof buf_or_n__$1 === 'number')?cljs.core.async.buffer.call(null,buf_or_n__$1):buf_or_n__$1),xform,ex_handler);
});

cljs.core.async.chan.cljs$lang$maxFixedArity = 3;
/**
 * Returns a channel that will close after msecs
 */
cljs.core.async.timeout = (function cljs$core$async$timeout(msecs){
return cljs.core.async.impl.timers.timeout.call(null,msecs);
});
/**
 * takes a val from port. Must be called inside a (go ...) block. Will
 * return nil if closed. Will park if nothing is available.
 * Returns true unless port is already closed
 */
cljs.core.async._LT__BANG_ = (function cljs$core$async$_LT__BANG_(port){
throw (new Error("<! used not in (go ...) block"));
});
/**
 * Asynchronously takes a val from port, passing to fn1. Will pass nil
 * if closed. If on-caller? (default true) is true, and value is
 * immediately available, will call fn1 on calling thread.
 * Returns nil.
 */
cljs.core.async.take_BANG_ = (function cljs$core$async$take_BANG_(){
var G__12034 = arguments.length;
switch (G__12034) {
case 2:
return cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$2 = (function (port,fn1){
return cljs.core.async.take_BANG_.call(null,port,fn1,true);
});

cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$3 = (function (port,fn1,on_caller_QMARK_){
var ret = cljs.core.async.impl.protocols.take_BANG_.call(null,port,cljs.core.async.fn_handler.call(null,fn1));
if(cljs.core.truth_(ret)){
var val_12036 = cljs.core.deref.call(null,ret);
if(cljs.core.truth_(on_caller_QMARK_)){
fn1.call(null,val_12036);
} else {
cljs.core.async.impl.dispatch.run.call(null,((function (val_12036,ret){
return (function (){
return fn1.call(null,val_12036);
});})(val_12036,ret))
);
}
} else {
}

return null;
});

cljs.core.async.take_BANG_.cljs$lang$maxFixedArity = 3;
cljs.core.async.nop = (function cljs$core$async$nop(_){
return null;
});
cljs.core.async.fhnop = cljs.core.async.fn_handler.call(null,cljs.core.async.nop);
/**
 * puts a val into port. nil values are not allowed. Must be called
 * inside a (go ...) block. Will park if no buffer space is available.
 * Returns true unless port is already closed.
 */
cljs.core.async._GT__BANG_ = (function cljs$core$async$_GT__BANG_(port,val){
throw (new Error(">! used not in (go ...) block"));
});
/**
 * Asynchronously puts a val into port, calling fn0 (if supplied) when
 * complete. nil values are not allowed. Will throw if closed. If
 * on-caller? (default true) is true, and the put is immediately
 * accepted, will call fn0 on calling thread.  Returns nil.
 */
cljs.core.async.put_BANG_ = (function cljs$core$async$put_BANG_(){
var G__12038 = arguments.length;
switch (G__12038) {
case 2:
return cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$2 = (function (port,val){
var temp__4423__auto__ = cljs.core.async.impl.protocols.put_BANG_.call(null,port,val,cljs.core.async.fhnop);
if(cljs.core.truth_(temp__4423__auto__)){
var ret = temp__4423__auto__;
return cljs.core.deref.call(null,ret);
} else {
return true;
}
});

cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$3 = (function (port,val,fn1){
return cljs.core.async.put_BANG_.call(null,port,val,fn1,true);
});

cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$4 = (function (port,val,fn1,on_caller_QMARK_){
var temp__4423__auto__ = cljs.core.async.impl.protocols.put_BANG_.call(null,port,val,cljs.core.async.fn_handler.call(null,fn1));
if(cljs.core.truth_(temp__4423__auto__)){
var retb = temp__4423__auto__;
var ret = cljs.core.deref.call(null,retb);
if(cljs.core.truth_(on_caller_QMARK_)){
fn1.call(null,ret);
} else {
cljs.core.async.impl.dispatch.run.call(null,((function (ret,retb,temp__4423__auto__){
return (function (){
return fn1.call(null,ret);
});})(ret,retb,temp__4423__auto__))
);
}

return ret;
} else {
return true;
}
});

cljs.core.async.put_BANG_.cljs$lang$maxFixedArity = 4;
cljs.core.async.close_BANG_ = (function cljs$core$async$close_BANG_(port){
return cljs.core.async.impl.protocols.close_BANG_.call(null,port);
});
cljs.core.async.random_array = (function cljs$core$async$random_array(n){
var a = (new Array(n));
var n__5206__auto___12040 = n;
var x_12041 = (0);
while(true){
if((x_12041 < n__5206__auto___12040)){
(a[x_12041] = (0));

var G__12042 = (x_12041 + (1));
x_12041 = G__12042;
continue;
} else {
}
break;
}

var i = (1);
while(true){
if(cljs.core._EQ_.call(null,i,n)){
return a;
} else {
var j = cljs.core.rand_int.call(null,i);
(a[i] = (a[j]));

(a[j] = i);

var G__12043 = (i + (1));
i = G__12043;
continue;
}
break;
}
});
cljs.core.async.alt_flag = (function cljs$core$async$alt_flag(){
var flag = cljs.core.atom.call(null,true);
if(typeof cljs.core.async.t12047 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t12047 = (function (alt_flag,flag,meta12048){
this.alt_flag = alt_flag;
this.flag = flag;
this.meta12048 = meta12048;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t12047.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (flag){
return (function (_12049,meta12048__$1){
var self__ = this;
var _12049__$1 = this;
return (new cljs.core.async.t12047(self__.alt_flag,self__.flag,meta12048__$1));
});})(flag))
;

cljs.core.async.t12047.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (flag){
return (function (_12049){
var self__ = this;
var _12049__$1 = this;
return self__.meta12048;
});})(flag))
;

cljs.core.async.t12047.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t12047.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = ((function (flag){
return (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.deref.call(null,self__.flag);
});})(flag))
;

cljs.core.async.t12047.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = ((function (flag){
return (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.reset_BANG_.call(null,self__.flag,null);

return true;
});})(flag))
;

cljs.core.async.t12047.getBasis = ((function (flag){
return (function (){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"alt-flag","alt-flag",-1794972754,null),new cljs.core.Symbol(null,"flag","flag",-1565787888,null),new cljs.core.Symbol(null,"meta12048","meta12048",-1138494948,null)], null);
});})(flag))
;

cljs.core.async.t12047.cljs$lang$type = true;

cljs.core.async.t12047.cljs$lang$ctorStr = "cljs.core.async/t12047";

cljs.core.async.t12047.cljs$lang$ctorPrWriter = ((function (flag){
return (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t12047");
});})(flag))
;

cljs.core.async.__GT_t12047 = ((function (flag){
return (function cljs$core$async$alt_flag_$___GT_t12047(alt_flag__$1,flag__$1,meta12048){
return (new cljs.core.async.t12047(alt_flag__$1,flag__$1,meta12048));
});})(flag))
;

}

return (new cljs.core.async.t12047(cljs$core$async$alt_flag,flag,cljs.core.PersistentArrayMap.EMPTY));
});
cljs.core.async.alt_handler = (function cljs$core$async$alt_handler(flag,cb){
if(typeof cljs.core.async.t12053 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t12053 = (function (alt_handler,flag,cb,meta12054){
this.alt_handler = alt_handler;
this.flag = flag;
this.cb = cb;
this.meta12054 = meta12054;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t12053.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_12055,meta12054__$1){
var self__ = this;
var _12055__$1 = this;
return (new cljs.core.async.t12053(self__.alt_handler,self__.flag,self__.cb,meta12054__$1));
});

cljs.core.async.t12053.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_12055){
var self__ = this;
var _12055__$1 = this;
return self__.meta12054;
});

cljs.core.async.t12053.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t12053.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.active_QMARK_.call(null,self__.flag);
});

cljs.core.async.t12053.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.async.impl.protocols.commit.call(null,self__.flag);

return self__.cb;
});

cljs.core.async.t12053.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"alt-handler","alt-handler",963786170,null),new cljs.core.Symbol(null,"flag","flag",-1565787888,null),new cljs.core.Symbol(null,"cb","cb",-2064487928,null),new cljs.core.Symbol(null,"meta12054","meta12054",-1771512183,null)], null);
});

cljs.core.async.t12053.cljs$lang$type = true;

cljs.core.async.t12053.cljs$lang$ctorStr = "cljs.core.async/t12053";

cljs.core.async.t12053.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t12053");
});

cljs.core.async.__GT_t12053 = (function cljs$core$async$alt_handler_$___GT_t12053(alt_handler__$1,flag__$1,cb__$1,meta12054){
return (new cljs.core.async.t12053(alt_handler__$1,flag__$1,cb__$1,meta12054));
});

}

return (new cljs.core.async.t12053(cljs$core$async$alt_handler,flag,cb,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * returns derefable [val port] if immediate, nil if enqueued
 */
cljs.core.async.do_alts = (function cljs$core$async$do_alts(fret,ports,opts){
var flag = cljs.core.async.alt_flag.call(null);
var n = cljs.core.count.call(null,ports);
var idxs = cljs.core.async.random_array.call(null,n);
var priority = new cljs.core.Keyword(null,"priority","priority",1431093715).cljs$core$IFn$_invoke$arity$1(opts);
var ret = (function (){var i = (0);
while(true){
if((i < n)){
var idx = (cljs.core.truth_(priority)?i:(idxs[i]));
var port = cljs.core.nth.call(null,ports,idx);
var wport = ((cljs.core.vector_QMARK_.call(null,port))?port.call(null,(0)):null);
var vbox = (cljs.core.truth_(wport)?(function (){var val = port.call(null,(1));
return cljs.core.async.impl.protocols.put_BANG_.call(null,wport,val,cljs.core.async.alt_handler.call(null,flag,((function (i,val,idx,port,wport,flag,n,idxs,priority){
return (function (p1__12056_SHARP_){
return fret.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [p1__12056_SHARP_,wport], null));
});})(i,val,idx,port,wport,flag,n,idxs,priority))
));
})():cljs.core.async.impl.protocols.take_BANG_.call(null,port,cljs.core.async.alt_handler.call(null,flag,((function (i,idx,port,wport,flag,n,idxs,priority){
return (function (p1__12057_SHARP_){
return fret.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [p1__12057_SHARP_,port], null));
});})(i,idx,port,wport,flag,n,idxs,priority))
)));
if(cljs.core.truth_(vbox)){
return cljs.core.async.impl.channels.box.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.deref.call(null,vbox),(function (){var or__4321__auto__ = wport;
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return port;
}
})()], null));
} else {
var G__12058 = (i + (1));
i = G__12058;
continue;
}
} else {
return null;
}
break;
}
})();
var or__4321__auto__ = ret;
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
if(cljs.core.contains_QMARK_.call(null,opts,new cljs.core.Keyword(null,"default","default",-1987822328))){
var temp__4425__auto__ = (function (){var and__4309__auto__ = cljs.core.async.impl.protocols.active_QMARK_.call(null,flag);
if(cljs.core.truth_(and__4309__auto__)){
return cljs.core.async.impl.protocols.commit.call(null,flag);
} else {
return and__4309__auto__;
}
})();
if(cljs.core.truth_(temp__4425__auto__)){
var got = temp__4425__auto__;
return cljs.core.async.impl.channels.box.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(opts),new cljs.core.Keyword(null,"default","default",-1987822328)], null));
} else {
return null;
}
} else {
return null;
}
}
});
/**
 * Completes at most one of several channel operations. Must be called
 * inside a (go ...) block. ports is a vector of channel endpoints,
 * which can be either a channel to take from or a vector of
 * [channel-to-put-to val-to-put], in any combination. Takes will be
 * made as if by <!, and puts will be made as if by >!. Unless
 * the :priority option is true, if more than one port operation is
 * ready a non-deterministic choice will be made. If no operation is
 * ready and a :default value is supplied, [default-val :default] will
 * be returned, otherwise alts! will park until the first operation to
 * become ready completes. Returns [val port] of the completed
 * operation, where val is the value taken for takes, and a
 * boolean (true unless already closed, as per put!) for puts.
 * 
 * opts are passed as :key val ... Supported options:
 * 
 * :default val - the value to use if none of the operations are immediately ready
 * :priority true - (default nil) when true, the operations will be tried in order.
 * 
 * Note: there is no guarantee that the port exps or val exprs will be
 * used, nor in what order should they be, so they should not be
 * depended upon for side effects.
 */
cljs.core.async.alts_BANG_ = (function cljs$core$async$alts_BANG_(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs.core.async.alts_BANG_.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs.core.async.alts_BANG_.cljs$core$IFn$_invoke$arity$variadic = (function (ports,p__12061){
var map__12062 = p__12061;
var map__12062__$1 = ((cljs.core.seq_QMARK_.call(null,map__12062))?cljs.core.apply.call(null,cljs.core.hash_map,map__12062):map__12062);
var opts = map__12062__$1;
throw (new Error("alts! used not in (go ...) block"));
});

cljs.core.async.alts_BANG_.cljs$lang$maxFixedArity = (1);

cljs.core.async.alts_BANG_.cljs$lang$applyTo = (function (seq12059){
var G__12060 = cljs.core.first.call(null,seq12059);
var seq12059__$1 = cljs.core.next.call(null,seq12059);
return cljs.core.async.alts_BANG_.cljs$core$IFn$_invoke$arity$variadic(G__12060,seq12059__$1);
});
/**
 * Takes elements from the from channel and supplies them to the to
 * channel. By default, the to channel will be closed when the from
 * channel closes, but can be determined by the close?  parameter. Will
 * stop consuming the from channel if the to channel closes
 */
cljs.core.async.pipe = (function cljs$core$async$pipe(){
var G__12064 = arguments.length;
switch (G__12064) {
case 2:
return cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$2 = (function (from,to){
return cljs.core.async.pipe.call(null,from,to,true);
});

cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$3 = (function (from,to,close_QMARK_){
var c__6803__auto___12113 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___12113){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___12113){
return (function (state_12088){
var state_val_12089 = (state_12088[(1)]);
if((state_val_12089 === (7))){
var inst_12084 = (state_12088[(2)]);
var state_12088__$1 = state_12088;
var statearr_12090_12114 = state_12088__$1;
(statearr_12090_12114[(2)] = inst_12084);

(statearr_12090_12114[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (1))){
var state_12088__$1 = state_12088;
var statearr_12091_12115 = state_12088__$1;
(statearr_12091_12115[(2)] = null);

(statearr_12091_12115[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (4))){
var inst_12067 = (state_12088[(7)]);
var inst_12067__$1 = (state_12088[(2)]);
var inst_12068 = (inst_12067__$1 == null);
var state_12088__$1 = (function (){var statearr_12092 = state_12088;
(statearr_12092[(7)] = inst_12067__$1);

return statearr_12092;
})();
if(cljs.core.truth_(inst_12068)){
var statearr_12093_12116 = state_12088__$1;
(statearr_12093_12116[(1)] = (5));

} else {
var statearr_12094_12117 = state_12088__$1;
(statearr_12094_12117[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (13))){
var state_12088__$1 = state_12088;
var statearr_12095_12118 = state_12088__$1;
(statearr_12095_12118[(2)] = null);

(statearr_12095_12118[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (6))){
var inst_12067 = (state_12088[(7)]);
var state_12088__$1 = state_12088;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_12088__$1,(11),to,inst_12067);
} else {
if((state_val_12089 === (3))){
var inst_12086 = (state_12088[(2)]);
var state_12088__$1 = state_12088;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12088__$1,inst_12086);
} else {
if((state_val_12089 === (12))){
var state_12088__$1 = state_12088;
var statearr_12096_12119 = state_12088__$1;
(statearr_12096_12119[(2)] = null);

(statearr_12096_12119[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (2))){
var state_12088__$1 = state_12088;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12088__$1,(4),from);
} else {
if((state_val_12089 === (11))){
var inst_12077 = (state_12088[(2)]);
var state_12088__$1 = state_12088;
if(cljs.core.truth_(inst_12077)){
var statearr_12097_12120 = state_12088__$1;
(statearr_12097_12120[(1)] = (12));

} else {
var statearr_12098_12121 = state_12088__$1;
(statearr_12098_12121[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (9))){
var state_12088__$1 = state_12088;
var statearr_12099_12122 = state_12088__$1;
(statearr_12099_12122[(2)] = null);

(statearr_12099_12122[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (5))){
var state_12088__$1 = state_12088;
if(cljs.core.truth_(close_QMARK_)){
var statearr_12100_12123 = state_12088__$1;
(statearr_12100_12123[(1)] = (8));

} else {
var statearr_12101_12124 = state_12088__$1;
(statearr_12101_12124[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (14))){
var inst_12082 = (state_12088[(2)]);
var state_12088__$1 = state_12088;
var statearr_12102_12125 = state_12088__$1;
(statearr_12102_12125[(2)] = inst_12082);

(statearr_12102_12125[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (10))){
var inst_12074 = (state_12088[(2)]);
var state_12088__$1 = state_12088;
var statearr_12103_12126 = state_12088__$1;
(statearr_12103_12126[(2)] = inst_12074);

(statearr_12103_12126[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12089 === (8))){
var inst_12071 = cljs.core.async.close_BANG_.call(null,to);
var state_12088__$1 = state_12088;
var statearr_12104_12127 = state_12088__$1;
(statearr_12104_12127[(2)] = inst_12071);

(statearr_12104_12127[(1)] = (10));


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
}
}
}
}
}
}
});})(c__6803__auto___12113))
;
return ((function (switch__6741__auto__,c__6803__auto___12113){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_12108 = [null,null,null,null,null,null,null,null];
(statearr_12108[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_12108[(1)] = (1));

return statearr_12108;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_12088){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12088);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12109){if((e12109 instanceof Object)){
var ex__6745__auto__ = e12109;
var statearr_12110_12128 = state_12088;
(statearr_12110_12128[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12088);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12109;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12129 = state_12088;
state_12088 = G__12129;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_12088){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_12088);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___12113))
})();
var state__6805__auto__ = (function (){var statearr_12111 = f__6804__auto__.call(null);
(statearr_12111[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___12113);

return statearr_12111;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___12113))
);


return to;
});

cljs.core.async.pipe.cljs$lang$maxFixedArity = 3;
cljs.core.async.pipeline_STAR_ = (function cljs$core$async$pipeline_STAR_(n,to,xf,from,close_QMARK_,ex_handler,type){
if((n > (0))){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol(null,"pos?","pos?",-244377722,null),new cljs.core.Symbol(null,"n","n",-2092305744,null))))].join('')));
}

var jobs = cljs.core.async.chan.call(null,n);
var results = cljs.core.async.chan.call(null,n);
var process = ((function (jobs,results){
return (function (p__12313){
var vec__12314 = p__12313;
var v = cljs.core.nth.call(null,vec__12314,(0),null);
var p = cljs.core.nth.call(null,vec__12314,(1),null);
var job = vec__12314;
if((job == null)){
cljs.core.async.close_BANG_.call(null,results);

return null;
} else {
var res = cljs.core.async.chan.call(null,(1),xf,ex_handler);
var c__6803__auto___12496 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___12496,res,vec__12314,v,p,job,jobs,results){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___12496,res,vec__12314,v,p,job,jobs,results){
return (function (state_12319){
var state_val_12320 = (state_12319[(1)]);
if((state_val_12320 === (1))){
var state_12319__$1 = state_12319;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_12319__$1,(2),res,v);
} else {
if((state_val_12320 === (2))){
var inst_12316 = (state_12319[(2)]);
var inst_12317 = cljs.core.async.close_BANG_.call(null,res);
var state_12319__$1 = (function (){var statearr_12321 = state_12319;
(statearr_12321[(7)] = inst_12316);

return statearr_12321;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12319__$1,inst_12317);
} else {
return null;
}
}
});})(c__6803__auto___12496,res,vec__12314,v,p,job,jobs,results))
;
return ((function (switch__6741__auto__,c__6803__auto___12496,res,vec__12314,v,p,job,jobs,results){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0 = (function (){
var statearr_12325 = [null,null,null,null,null,null,null,null];
(statearr_12325[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__);

(statearr_12325[(1)] = (1));

return statearr_12325;
});
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1 = (function (state_12319){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12319);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12326){if((e12326 instanceof Object)){
var ex__6745__auto__ = e12326;
var statearr_12327_12497 = state_12319;
(statearr_12327_12497[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12319);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12326;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12498 = state_12319;
state_12319 = G__12498;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = function(state_12319){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1.call(this,state_12319);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___12496,res,vec__12314,v,p,job,jobs,results))
})();
var state__6805__auto__ = (function (){var statearr_12328 = f__6804__auto__.call(null);
(statearr_12328[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___12496);

return statearr_12328;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___12496,res,vec__12314,v,p,job,jobs,results))
);


cljs.core.async.put_BANG_.call(null,p,res);

return true;
}
});})(jobs,results))
;
var async = ((function (jobs,results,process){
return (function (p__12329){
var vec__12330 = p__12329;
var v = cljs.core.nth.call(null,vec__12330,(0),null);
var p = cljs.core.nth.call(null,vec__12330,(1),null);
var job = vec__12330;
if((job == null)){
cljs.core.async.close_BANG_.call(null,results);

return null;
} else {
var res = cljs.core.async.chan.call(null,(1));
xf.call(null,v,res);

cljs.core.async.put_BANG_.call(null,p,res);

return true;
}
});})(jobs,results,process))
;
var n__5206__auto___12499 = n;
var __12500 = (0);
while(true){
if((__12500 < n__5206__auto___12499)){
var G__12331_12501 = (((type instanceof cljs.core.Keyword))?type.fqn:null);
switch (G__12331_12501) {
case "compute":
var c__6803__auto___12503 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (__12500,c__6803__auto___12503,G__12331_12501,n__5206__auto___12499,jobs,results,process,async){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (__12500,c__6803__auto___12503,G__12331_12501,n__5206__auto___12499,jobs,results,process,async){
return (function (state_12344){
var state_val_12345 = (state_12344[(1)]);
if((state_val_12345 === (1))){
var state_12344__$1 = state_12344;
var statearr_12346_12504 = state_12344__$1;
(statearr_12346_12504[(2)] = null);

(statearr_12346_12504[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12345 === (2))){
var state_12344__$1 = state_12344;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12344__$1,(4),jobs);
} else {
if((state_val_12345 === (3))){
var inst_12342 = (state_12344[(2)]);
var state_12344__$1 = state_12344;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12344__$1,inst_12342);
} else {
if((state_val_12345 === (4))){
var inst_12334 = (state_12344[(2)]);
var inst_12335 = process.call(null,inst_12334);
var state_12344__$1 = state_12344;
if(cljs.core.truth_(inst_12335)){
var statearr_12347_12505 = state_12344__$1;
(statearr_12347_12505[(1)] = (5));

} else {
var statearr_12348_12506 = state_12344__$1;
(statearr_12348_12506[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12345 === (5))){
var state_12344__$1 = state_12344;
var statearr_12349_12507 = state_12344__$1;
(statearr_12349_12507[(2)] = null);

(statearr_12349_12507[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12345 === (6))){
var state_12344__$1 = state_12344;
var statearr_12350_12508 = state_12344__$1;
(statearr_12350_12508[(2)] = null);

(statearr_12350_12508[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12345 === (7))){
var inst_12340 = (state_12344[(2)]);
var state_12344__$1 = state_12344;
var statearr_12351_12509 = state_12344__$1;
(statearr_12351_12509[(2)] = inst_12340);

(statearr_12351_12509[(1)] = (3));


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
});})(__12500,c__6803__auto___12503,G__12331_12501,n__5206__auto___12499,jobs,results,process,async))
;
return ((function (__12500,switch__6741__auto__,c__6803__auto___12503,G__12331_12501,n__5206__auto___12499,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0 = (function (){
var statearr_12355 = [null,null,null,null,null,null,null];
(statearr_12355[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__);

(statearr_12355[(1)] = (1));

return statearr_12355;
});
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1 = (function (state_12344){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12344);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12356){if((e12356 instanceof Object)){
var ex__6745__auto__ = e12356;
var statearr_12357_12510 = state_12344;
(statearr_12357_12510[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12344);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12356;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12511 = state_12344;
state_12344 = G__12511;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = function(state_12344){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1.call(this,state_12344);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__;
})()
;})(__12500,switch__6741__auto__,c__6803__auto___12503,G__12331_12501,n__5206__auto___12499,jobs,results,process,async))
})();
var state__6805__auto__ = (function (){var statearr_12358 = f__6804__auto__.call(null);
(statearr_12358[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___12503);

return statearr_12358;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(__12500,c__6803__auto___12503,G__12331_12501,n__5206__auto___12499,jobs,results,process,async))
);


break;
case "async":
var c__6803__auto___12512 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (__12500,c__6803__auto___12512,G__12331_12501,n__5206__auto___12499,jobs,results,process,async){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (__12500,c__6803__auto___12512,G__12331_12501,n__5206__auto___12499,jobs,results,process,async){
return (function (state_12371){
var state_val_12372 = (state_12371[(1)]);
if((state_val_12372 === (1))){
var state_12371__$1 = state_12371;
var statearr_12373_12513 = state_12371__$1;
(statearr_12373_12513[(2)] = null);

(statearr_12373_12513[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12372 === (2))){
var state_12371__$1 = state_12371;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12371__$1,(4),jobs);
} else {
if((state_val_12372 === (3))){
var inst_12369 = (state_12371[(2)]);
var state_12371__$1 = state_12371;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12371__$1,inst_12369);
} else {
if((state_val_12372 === (4))){
var inst_12361 = (state_12371[(2)]);
var inst_12362 = async.call(null,inst_12361);
var state_12371__$1 = state_12371;
if(cljs.core.truth_(inst_12362)){
var statearr_12374_12514 = state_12371__$1;
(statearr_12374_12514[(1)] = (5));

} else {
var statearr_12375_12515 = state_12371__$1;
(statearr_12375_12515[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12372 === (5))){
var state_12371__$1 = state_12371;
var statearr_12376_12516 = state_12371__$1;
(statearr_12376_12516[(2)] = null);

(statearr_12376_12516[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12372 === (6))){
var state_12371__$1 = state_12371;
var statearr_12377_12517 = state_12371__$1;
(statearr_12377_12517[(2)] = null);

(statearr_12377_12517[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12372 === (7))){
var inst_12367 = (state_12371[(2)]);
var state_12371__$1 = state_12371;
var statearr_12378_12518 = state_12371__$1;
(statearr_12378_12518[(2)] = inst_12367);

(statearr_12378_12518[(1)] = (3));


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
});})(__12500,c__6803__auto___12512,G__12331_12501,n__5206__auto___12499,jobs,results,process,async))
;
return ((function (__12500,switch__6741__auto__,c__6803__auto___12512,G__12331_12501,n__5206__auto___12499,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0 = (function (){
var statearr_12382 = [null,null,null,null,null,null,null];
(statearr_12382[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__);

(statearr_12382[(1)] = (1));

return statearr_12382;
});
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1 = (function (state_12371){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12371);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12383){if((e12383 instanceof Object)){
var ex__6745__auto__ = e12383;
var statearr_12384_12519 = state_12371;
(statearr_12384_12519[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12371);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12383;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12520 = state_12371;
state_12371 = G__12520;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = function(state_12371){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1.call(this,state_12371);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__;
})()
;})(__12500,switch__6741__auto__,c__6803__auto___12512,G__12331_12501,n__5206__auto___12499,jobs,results,process,async))
})();
var state__6805__auto__ = (function (){var statearr_12385 = f__6804__auto__.call(null);
(statearr_12385[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___12512);

return statearr_12385;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(__12500,c__6803__auto___12512,G__12331_12501,n__5206__auto___12499,jobs,results,process,async))
);


break;
default:
throw (new Error([cljs.core.str("No matching clause: "),cljs.core.str(type)].join('')));

}

var G__12521 = (__12500 + (1));
__12500 = G__12521;
continue;
} else {
}
break;
}

var c__6803__auto___12522 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___12522,jobs,results,process,async){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___12522,jobs,results,process,async){
return (function (state_12407){
var state_val_12408 = (state_12407[(1)]);
if((state_val_12408 === (1))){
var state_12407__$1 = state_12407;
var statearr_12409_12523 = state_12407__$1;
(statearr_12409_12523[(2)] = null);

(statearr_12409_12523[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12408 === (2))){
var state_12407__$1 = state_12407;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12407__$1,(4),from);
} else {
if((state_val_12408 === (3))){
var inst_12405 = (state_12407[(2)]);
var state_12407__$1 = state_12407;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12407__$1,inst_12405);
} else {
if((state_val_12408 === (4))){
var inst_12388 = (state_12407[(7)]);
var inst_12388__$1 = (state_12407[(2)]);
var inst_12389 = (inst_12388__$1 == null);
var state_12407__$1 = (function (){var statearr_12410 = state_12407;
(statearr_12410[(7)] = inst_12388__$1);

return statearr_12410;
})();
if(cljs.core.truth_(inst_12389)){
var statearr_12411_12524 = state_12407__$1;
(statearr_12411_12524[(1)] = (5));

} else {
var statearr_12412_12525 = state_12407__$1;
(statearr_12412_12525[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12408 === (5))){
var inst_12391 = cljs.core.async.close_BANG_.call(null,jobs);
var state_12407__$1 = state_12407;
var statearr_12413_12526 = state_12407__$1;
(statearr_12413_12526[(2)] = inst_12391);

(statearr_12413_12526[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12408 === (6))){
var inst_12388 = (state_12407[(7)]);
var inst_12393 = (state_12407[(8)]);
var inst_12393__$1 = cljs.core.async.chan.call(null,(1));
var inst_12394 = cljs.core.PersistentVector.EMPTY_NODE;
var inst_12395 = [inst_12388,inst_12393__$1];
var inst_12396 = (new cljs.core.PersistentVector(null,2,(5),inst_12394,inst_12395,null));
var state_12407__$1 = (function (){var statearr_12414 = state_12407;
(statearr_12414[(8)] = inst_12393__$1);

return statearr_12414;
})();
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_12407__$1,(8),jobs,inst_12396);
} else {
if((state_val_12408 === (7))){
var inst_12403 = (state_12407[(2)]);
var state_12407__$1 = state_12407;
var statearr_12415_12527 = state_12407__$1;
(statearr_12415_12527[(2)] = inst_12403);

(statearr_12415_12527[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12408 === (8))){
var inst_12393 = (state_12407[(8)]);
var inst_12398 = (state_12407[(2)]);
var state_12407__$1 = (function (){var statearr_12416 = state_12407;
(statearr_12416[(9)] = inst_12398);

return statearr_12416;
})();
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_12407__$1,(9),results,inst_12393);
} else {
if((state_val_12408 === (9))){
var inst_12400 = (state_12407[(2)]);
var state_12407__$1 = (function (){var statearr_12417 = state_12407;
(statearr_12417[(10)] = inst_12400);

return statearr_12417;
})();
var statearr_12418_12528 = state_12407__$1;
(statearr_12418_12528[(2)] = null);

(statearr_12418_12528[(1)] = (2));


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
}
});})(c__6803__auto___12522,jobs,results,process,async))
;
return ((function (switch__6741__auto__,c__6803__auto___12522,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0 = (function (){
var statearr_12422 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_12422[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__);

(statearr_12422[(1)] = (1));

return statearr_12422;
});
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1 = (function (state_12407){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12407);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12423){if((e12423 instanceof Object)){
var ex__6745__auto__ = e12423;
var statearr_12424_12529 = state_12407;
(statearr_12424_12529[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12407);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12423;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12530 = state_12407;
state_12407 = G__12530;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = function(state_12407){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1.call(this,state_12407);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___12522,jobs,results,process,async))
})();
var state__6805__auto__ = (function (){var statearr_12425 = f__6804__auto__.call(null);
(statearr_12425[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___12522);

return statearr_12425;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___12522,jobs,results,process,async))
);


var c__6803__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto__,jobs,results,process,async){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto__,jobs,results,process,async){
return (function (state_12463){
var state_val_12464 = (state_12463[(1)]);
if((state_val_12464 === (7))){
var inst_12459 = (state_12463[(2)]);
var state_12463__$1 = state_12463;
var statearr_12465_12531 = state_12463__$1;
(statearr_12465_12531[(2)] = inst_12459);

(statearr_12465_12531[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (20))){
var state_12463__$1 = state_12463;
var statearr_12466_12532 = state_12463__$1;
(statearr_12466_12532[(2)] = null);

(statearr_12466_12532[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (1))){
var state_12463__$1 = state_12463;
var statearr_12467_12533 = state_12463__$1;
(statearr_12467_12533[(2)] = null);

(statearr_12467_12533[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (4))){
var inst_12428 = (state_12463[(7)]);
var inst_12428__$1 = (state_12463[(2)]);
var inst_12429 = (inst_12428__$1 == null);
var state_12463__$1 = (function (){var statearr_12468 = state_12463;
(statearr_12468[(7)] = inst_12428__$1);

return statearr_12468;
})();
if(cljs.core.truth_(inst_12429)){
var statearr_12469_12534 = state_12463__$1;
(statearr_12469_12534[(1)] = (5));

} else {
var statearr_12470_12535 = state_12463__$1;
(statearr_12470_12535[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (15))){
var inst_12441 = (state_12463[(8)]);
var state_12463__$1 = state_12463;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_12463__$1,(18),to,inst_12441);
} else {
if((state_val_12464 === (21))){
var inst_12454 = (state_12463[(2)]);
var state_12463__$1 = state_12463;
var statearr_12471_12536 = state_12463__$1;
(statearr_12471_12536[(2)] = inst_12454);

(statearr_12471_12536[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (13))){
var inst_12456 = (state_12463[(2)]);
var state_12463__$1 = (function (){var statearr_12472 = state_12463;
(statearr_12472[(9)] = inst_12456);

return statearr_12472;
})();
var statearr_12473_12537 = state_12463__$1;
(statearr_12473_12537[(2)] = null);

(statearr_12473_12537[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (6))){
var inst_12428 = (state_12463[(7)]);
var state_12463__$1 = state_12463;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12463__$1,(11),inst_12428);
} else {
if((state_val_12464 === (17))){
var inst_12449 = (state_12463[(2)]);
var state_12463__$1 = state_12463;
if(cljs.core.truth_(inst_12449)){
var statearr_12474_12538 = state_12463__$1;
(statearr_12474_12538[(1)] = (19));

} else {
var statearr_12475_12539 = state_12463__$1;
(statearr_12475_12539[(1)] = (20));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (3))){
var inst_12461 = (state_12463[(2)]);
var state_12463__$1 = state_12463;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12463__$1,inst_12461);
} else {
if((state_val_12464 === (12))){
var inst_12438 = (state_12463[(10)]);
var state_12463__$1 = state_12463;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12463__$1,(14),inst_12438);
} else {
if((state_val_12464 === (2))){
var state_12463__$1 = state_12463;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12463__$1,(4),results);
} else {
if((state_val_12464 === (19))){
var state_12463__$1 = state_12463;
var statearr_12476_12540 = state_12463__$1;
(statearr_12476_12540[(2)] = null);

(statearr_12476_12540[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (11))){
var inst_12438 = (state_12463[(2)]);
var state_12463__$1 = (function (){var statearr_12477 = state_12463;
(statearr_12477[(10)] = inst_12438);

return statearr_12477;
})();
var statearr_12478_12541 = state_12463__$1;
(statearr_12478_12541[(2)] = null);

(statearr_12478_12541[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (9))){
var state_12463__$1 = state_12463;
var statearr_12479_12542 = state_12463__$1;
(statearr_12479_12542[(2)] = null);

(statearr_12479_12542[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (5))){
var state_12463__$1 = state_12463;
if(cljs.core.truth_(close_QMARK_)){
var statearr_12480_12543 = state_12463__$1;
(statearr_12480_12543[(1)] = (8));

} else {
var statearr_12481_12544 = state_12463__$1;
(statearr_12481_12544[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (14))){
var inst_12443 = (state_12463[(11)]);
var inst_12441 = (state_12463[(8)]);
var inst_12441__$1 = (state_12463[(2)]);
var inst_12442 = (inst_12441__$1 == null);
var inst_12443__$1 = cljs.core.not.call(null,inst_12442);
var state_12463__$1 = (function (){var statearr_12482 = state_12463;
(statearr_12482[(11)] = inst_12443__$1);

(statearr_12482[(8)] = inst_12441__$1);

return statearr_12482;
})();
if(inst_12443__$1){
var statearr_12483_12545 = state_12463__$1;
(statearr_12483_12545[(1)] = (15));

} else {
var statearr_12484_12546 = state_12463__$1;
(statearr_12484_12546[(1)] = (16));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (16))){
var inst_12443 = (state_12463[(11)]);
var state_12463__$1 = state_12463;
var statearr_12485_12547 = state_12463__$1;
(statearr_12485_12547[(2)] = inst_12443);

(statearr_12485_12547[(1)] = (17));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (10))){
var inst_12435 = (state_12463[(2)]);
var state_12463__$1 = state_12463;
var statearr_12486_12548 = state_12463__$1;
(statearr_12486_12548[(2)] = inst_12435);

(statearr_12486_12548[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (18))){
var inst_12446 = (state_12463[(2)]);
var state_12463__$1 = state_12463;
var statearr_12487_12549 = state_12463__$1;
(statearr_12487_12549[(2)] = inst_12446);

(statearr_12487_12549[(1)] = (17));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12464 === (8))){
var inst_12432 = cljs.core.async.close_BANG_.call(null,to);
var state_12463__$1 = state_12463;
var statearr_12488_12550 = state_12463__$1;
(statearr_12488_12550[(2)] = inst_12432);

(statearr_12488_12550[(1)] = (10));


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
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__6803__auto__,jobs,results,process,async))
;
return ((function (switch__6741__auto__,c__6803__auto__,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0 = (function (){
var statearr_12492 = [null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_12492[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__);

(statearr_12492[(1)] = (1));

return statearr_12492;
});
var cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1 = (function (state_12463){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12463);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12493){if((e12493 instanceof Object)){
var ex__6745__auto__ = e12493;
var statearr_12494_12551 = state_12463;
(statearr_12494_12551[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12463);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12493;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12552 = state_12463;
state_12463 = G__12552;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__ = function(state_12463){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1.call(this,state_12463);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__6742__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto__,jobs,results,process,async))
})();
var state__6805__auto__ = (function (){var statearr_12495 = f__6804__auto__.call(null);
(statearr_12495[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto__);

return statearr_12495;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto__,jobs,results,process,async))
);

return c__6803__auto__;
});
/**
 * Takes elements from the from channel and supplies them to the to
 * channel, subject to the async function af, with parallelism n. af
 * must be a function of two arguments, the first an input value and
 * the second a channel on which to place the result(s). af must close!
 * the channel before returning.  The presumption is that af will
 * return immediately, having launched some asynchronous operation
 * whose completion/callback will manipulate the result channel. Outputs
 * will be returned in order relative to  the inputs. By default, the to
 * channel will be closed when the from channel closes, but can be
 * determined by the close?  parameter. Will stop consuming the from
 * channel if the to channel closes.
 */
cljs.core.async.pipeline_async = (function cljs$core$async$pipeline_async(){
var G__12554 = arguments.length;
switch (G__12554) {
case 4:
return cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
case 5:
return cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$5((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$4 = (function (n,to,af,from){
return cljs.core.async.pipeline_async.call(null,n,to,af,from,true);
});

cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$5 = (function (n,to,af,from,close_QMARK_){
return cljs.core.async.pipeline_STAR_.call(null,n,to,af,from,close_QMARK_,null,new cljs.core.Keyword(null,"async","async",1050769601));
});

cljs.core.async.pipeline_async.cljs$lang$maxFixedArity = 5;
/**
 * Takes elements from the from channel and supplies them to the to
 * channel, subject to the transducer xf, with parallelism n. Because
 * it is parallel, the transducer will be applied independently to each
 * element, not across elements, and may produce zero or more outputs
 * per input.  Outputs will be returned in order relative to the
 * inputs. By default, the to channel will be closed when the from
 * channel closes, but can be determined by the close?  parameter. Will
 * stop consuming the from channel if the to channel closes.
 * 
 * Note this is supplied for API compatibility with the Clojure version.
 * Values of N > 1 will not result in actual concurrency in a
 * single-threaded runtime.
 */
cljs.core.async.pipeline = (function cljs$core$async$pipeline(){
var G__12557 = arguments.length;
switch (G__12557) {
case 4:
return cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
case 5:
return cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$5((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]));

break;
case 6:
return cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$6((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]),(arguments[(5)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$4 = (function (n,to,xf,from){
return cljs.core.async.pipeline.call(null,n,to,xf,from,true);
});

cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$5 = (function (n,to,xf,from,close_QMARK_){
return cljs.core.async.pipeline.call(null,n,to,xf,from,close_QMARK_,null);
});

cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$6 = (function (n,to,xf,from,close_QMARK_,ex_handler){
return cljs.core.async.pipeline_STAR_.call(null,n,to,xf,from,close_QMARK_,ex_handler,new cljs.core.Keyword(null,"compute","compute",1555393130));
});

cljs.core.async.pipeline.cljs$lang$maxFixedArity = 6;
/**
 * Takes a predicate and a source channel and returns a vector of two
 * channels, the first of which will contain the values for which the
 * predicate returned true, the second those for which it returned
 * false.
 * 
 * The out channels will be unbuffered by default, or two buf-or-ns can
 * be supplied. The channels will close after the source channel has
 * closed.
 */
cljs.core.async.split = (function cljs$core$async$split(){
var G__12560 = arguments.length;
switch (G__12560) {
case 2:
return cljs.core.async.split.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 4:
return cljs.core.async.split.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.split.cljs$core$IFn$_invoke$arity$2 = (function (p,ch){
return cljs.core.async.split.call(null,p,ch,null,null);
});

cljs.core.async.split.cljs$core$IFn$_invoke$arity$4 = (function (p,ch,t_buf_or_n,f_buf_or_n){
var tc = cljs.core.async.chan.call(null,t_buf_or_n);
var fc = cljs.core.async.chan.call(null,f_buf_or_n);
var c__6803__auto___12612 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___12612,tc,fc){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___12612,tc,fc){
return (function (state_12586){
var state_val_12587 = (state_12586[(1)]);
if((state_val_12587 === (7))){
var inst_12582 = (state_12586[(2)]);
var state_12586__$1 = state_12586;
var statearr_12588_12613 = state_12586__$1;
(statearr_12588_12613[(2)] = inst_12582);

(statearr_12588_12613[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (1))){
var state_12586__$1 = state_12586;
var statearr_12589_12614 = state_12586__$1;
(statearr_12589_12614[(2)] = null);

(statearr_12589_12614[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (4))){
var inst_12563 = (state_12586[(7)]);
var inst_12563__$1 = (state_12586[(2)]);
var inst_12564 = (inst_12563__$1 == null);
var state_12586__$1 = (function (){var statearr_12590 = state_12586;
(statearr_12590[(7)] = inst_12563__$1);

return statearr_12590;
})();
if(cljs.core.truth_(inst_12564)){
var statearr_12591_12615 = state_12586__$1;
(statearr_12591_12615[(1)] = (5));

} else {
var statearr_12592_12616 = state_12586__$1;
(statearr_12592_12616[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (13))){
var state_12586__$1 = state_12586;
var statearr_12593_12617 = state_12586__$1;
(statearr_12593_12617[(2)] = null);

(statearr_12593_12617[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (6))){
var inst_12563 = (state_12586[(7)]);
var inst_12569 = p.call(null,inst_12563);
var state_12586__$1 = state_12586;
if(cljs.core.truth_(inst_12569)){
var statearr_12594_12618 = state_12586__$1;
(statearr_12594_12618[(1)] = (9));

} else {
var statearr_12595_12619 = state_12586__$1;
(statearr_12595_12619[(1)] = (10));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (3))){
var inst_12584 = (state_12586[(2)]);
var state_12586__$1 = state_12586;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12586__$1,inst_12584);
} else {
if((state_val_12587 === (12))){
var state_12586__$1 = state_12586;
var statearr_12596_12620 = state_12586__$1;
(statearr_12596_12620[(2)] = null);

(statearr_12596_12620[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (2))){
var state_12586__$1 = state_12586;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12586__$1,(4),ch);
} else {
if((state_val_12587 === (11))){
var inst_12563 = (state_12586[(7)]);
var inst_12573 = (state_12586[(2)]);
var state_12586__$1 = state_12586;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_12586__$1,(8),inst_12573,inst_12563);
} else {
if((state_val_12587 === (9))){
var state_12586__$1 = state_12586;
var statearr_12597_12621 = state_12586__$1;
(statearr_12597_12621[(2)] = tc);

(statearr_12597_12621[(1)] = (11));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (5))){
var inst_12566 = cljs.core.async.close_BANG_.call(null,tc);
var inst_12567 = cljs.core.async.close_BANG_.call(null,fc);
var state_12586__$1 = (function (){var statearr_12598 = state_12586;
(statearr_12598[(8)] = inst_12566);

return statearr_12598;
})();
var statearr_12599_12622 = state_12586__$1;
(statearr_12599_12622[(2)] = inst_12567);

(statearr_12599_12622[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (14))){
var inst_12580 = (state_12586[(2)]);
var state_12586__$1 = state_12586;
var statearr_12600_12623 = state_12586__$1;
(statearr_12600_12623[(2)] = inst_12580);

(statearr_12600_12623[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (10))){
var state_12586__$1 = state_12586;
var statearr_12601_12624 = state_12586__$1;
(statearr_12601_12624[(2)] = fc);

(statearr_12601_12624[(1)] = (11));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12587 === (8))){
var inst_12575 = (state_12586[(2)]);
var state_12586__$1 = state_12586;
if(cljs.core.truth_(inst_12575)){
var statearr_12602_12625 = state_12586__$1;
(statearr_12602_12625[(1)] = (12));

} else {
var statearr_12603_12626 = state_12586__$1;
(statearr_12603_12626[(1)] = (13));

}

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
}
}
}
}
}
}
});})(c__6803__auto___12612,tc,fc))
;
return ((function (switch__6741__auto__,c__6803__auto___12612,tc,fc){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_12607 = [null,null,null,null,null,null,null,null,null];
(statearr_12607[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_12607[(1)] = (1));

return statearr_12607;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_12586){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12586);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12608){if((e12608 instanceof Object)){
var ex__6745__auto__ = e12608;
var statearr_12609_12627 = state_12586;
(statearr_12609_12627[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12586);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12608;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12628 = state_12586;
state_12586 = G__12628;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_12586){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_12586);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___12612,tc,fc))
})();
var state__6805__auto__ = (function (){var statearr_12610 = f__6804__auto__.call(null);
(statearr_12610[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___12612);

return statearr_12610;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___12612,tc,fc))
);


return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [tc,fc], null);
});

cljs.core.async.split.cljs$lang$maxFixedArity = 4;
/**
 * f should be a function of 2 arguments. Returns a channel containing
 * the single result of applying f to init and the first item from the
 * channel, then applying f to that result and the 2nd item, etc. If
 * the channel closes without yielding items, returns init and f is not
 * called. ch must close before reduce produces a result.
 */
cljs.core.async.reduce = (function cljs$core$async$reduce(f,init,ch){
var c__6803__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto__){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto__){
return (function (state_12675){
var state_val_12676 = (state_12675[(1)]);
if((state_val_12676 === (1))){
var inst_12661 = init;
var state_12675__$1 = (function (){var statearr_12677 = state_12675;
(statearr_12677[(7)] = inst_12661);

return statearr_12677;
})();
var statearr_12678_12693 = state_12675__$1;
(statearr_12678_12693[(2)] = null);

(statearr_12678_12693[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12676 === (2))){
var state_12675__$1 = state_12675;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_12675__$1,(4),ch);
} else {
if((state_val_12676 === (3))){
var inst_12673 = (state_12675[(2)]);
var state_12675__$1 = state_12675;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12675__$1,inst_12673);
} else {
if((state_val_12676 === (4))){
var inst_12664 = (state_12675[(8)]);
var inst_12664__$1 = (state_12675[(2)]);
var inst_12665 = (inst_12664__$1 == null);
var state_12675__$1 = (function (){var statearr_12679 = state_12675;
(statearr_12679[(8)] = inst_12664__$1);

return statearr_12679;
})();
if(cljs.core.truth_(inst_12665)){
var statearr_12680_12694 = state_12675__$1;
(statearr_12680_12694[(1)] = (5));

} else {
var statearr_12681_12695 = state_12675__$1;
(statearr_12681_12695[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12676 === (5))){
var inst_12661 = (state_12675[(7)]);
var state_12675__$1 = state_12675;
var statearr_12682_12696 = state_12675__$1;
(statearr_12682_12696[(2)] = inst_12661);

(statearr_12682_12696[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12676 === (6))){
var inst_12661 = (state_12675[(7)]);
var inst_12664 = (state_12675[(8)]);
var inst_12668 = f.call(null,inst_12661,inst_12664);
var inst_12661__$1 = inst_12668;
var state_12675__$1 = (function (){var statearr_12683 = state_12675;
(statearr_12683[(7)] = inst_12661__$1);

return statearr_12683;
})();
var statearr_12684_12697 = state_12675__$1;
(statearr_12684_12697[(2)] = null);

(statearr_12684_12697[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12676 === (7))){
var inst_12671 = (state_12675[(2)]);
var state_12675__$1 = state_12675;
var statearr_12685_12698 = state_12675__$1;
(statearr_12685_12698[(2)] = inst_12671);

(statearr_12685_12698[(1)] = (3));


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
});})(c__6803__auto__))
;
return ((function (switch__6741__auto__,c__6803__auto__){
return (function() {
var cljs$core$async$reduce_$_state_machine__6742__auto__ = null;
var cljs$core$async$reduce_$_state_machine__6742__auto____0 = (function (){
var statearr_12689 = [null,null,null,null,null,null,null,null,null];
(statearr_12689[(0)] = cljs$core$async$reduce_$_state_machine__6742__auto__);

(statearr_12689[(1)] = (1));

return statearr_12689;
});
var cljs$core$async$reduce_$_state_machine__6742__auto____1 = (function (state_12675){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12675);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12690){if((e12690 instanceof Object)){
var ex__6745__auto__ = e12690;
var statearr_12691_12699 = state_12675;
(statearr_12691_12699[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12675);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12690;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12700 = state_12675;
state_12675 = G__12700;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$reduce_$_state_machine__6742__auto__ = function(state_12675){
switch(arguments.length){
case 0:
return cljs$core$async$reduce_$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$reduce_$_state_machine__6742__auto____1.call(this,state_12675);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$reduce_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$reduce_$_state_machine__6742__auto____0;
cljs$core$async$reduce_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$reduce_$_state_machine__6742__auto____1;
return cljs$core$async$reduce_$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto__))
})();
var state__6805__auto__ = (function (){var statearr_12692 = f__6804__auto__.call(null);
(statearr_12692[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto__);

return statearr_12692;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto__))
);

return c__6803__auto__;
});
/**
 * Puts the contents of coll into the supplied channel.
 * 
 * By default the channel will be closed after the items are copied,
 * but can be determined by the close? parameter.
 * 
 * Returns a channel which will close after the items are copied.
 */
cljs.core.async.onto_chan = (function cljs$core$async$onto_chan(){
var G__12702 = arguments.length;
switch (G__12702) {
case 2:
return cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$2 = (function (ch,coll){
return cljs.core.async.onto_chan.call(null,ch,coll,true);
});

cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$3 = (function (ch,coll,close_QMARK_){
var c__6803__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto__){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto__){
return (function (state_12727){
var state_val_12728 = (state_12727[(1)]);
if((state_val_12728 === (7))){
var inst_12709 = (state_12727[(2)]);
var state_12727__$1 = state_12727;
var statearr_12729_12753 = state_12727__$1;
(statearr_12729_12753[(2)] = inst_12709);

(statearr_12729_12753[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (1))){
var inst_12703 = cljs.core.seq.call(null,coll);
var inst_12704 = inst_12703;
var state_12727__$1 = (function (){var statearr_12730 = state_12727;
(statearr_12730[(7)] = inst_12704);

return statearr_12730;
})();
var statearr_12731_12754 = state_12727__$1;
(statearr_12731_12754[(2)] = null);

(statearr_12731_12754[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (4))){
var inst_12704 = (state_12727[(7)]);
var inst_12707 = cljs.core.first.call(null,inst_12704);
var state_12727__$1 = state_12727;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_12727__$1,(7),ch,inst_12707);
} else {
if((state_val_12728 === (13))){
var inst_12721 = (state_12727[(2)]);
var state_12727__$1 = state_12727;
var statearr_12732_12755 = state_12727__$1;
(statearr_12732_12755[(2)] = inst_12721);

(statearr_12732_12755[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (6))){
var inst_12712 = (state_12727[(2)]);
var state_12727__$1 = state_12727;
if(cljs.core.truth_(inst_12712)){
var statearr_12733_12756 = state_12727__$1;
(statearr_12733_12756[(1)] = (8));

} else {
var statearr_12734_12757 = state_12727__$1;
(statearr_12734_12757[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (3))){
var inst_12725 = (state_12727[(2)]);
var state_12727__$1 = state_12727;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_12727__$1,inst_12725);
} else {
if((state_val_12728 === (12))){
var state_12727__$1 = state_12727;
var statearr_12735_12758 = state_12727__$1;
(statearr_12735_12758[(2)] = null);

(statearr_12735_12758[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (2))){
var inst_12704 = (state_12727[(7)]);
var state_12727__$1 = state_12727;
if(cljs.core.truth_(inst_12704)){
var statearr_12736_12759 = state_12727__$1;
(statearr_12736_12759[(1)] = (4));

} else {
var statearr_12737_12760 = state_12727__$1;
(statearr_12737_12760[(1)] = (5));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (11))){
var inst_12718 = cljs.core.async.close_BANG_.call(null,ch);
var state_12727__$1 = state_12727;
var statearr_12738_12761 = state_12727__$1;
(statearr_12738_12761[(2)] = inst_12718);

(statearr_12738_12761[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (9))){
var state_12727__$1 = state_12727;
if(cljs.core.truth_(close_QMARK_)){
var statearr_12739_12762 = state_12727__$1;
(statearr_12739_12762[(1)] = (11));

} else {
var statearr_12740_12763 = state_12727__$1;
(statearr_12740_12763[(1)] = (12));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (5))){
var inst_12704 = (state_12727[(7)]);
var state_12727__$1 = state_12727;
var statearr_12741_12764 = state_12727__$1;
(statearr_12741_12764[(2)] = inst_12704);

(statearr_12741_12764[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (10))){
var inst_12723 = (state_12727[(2)]);
var state_12727__$1 = state_12727;
var statearr_12742_12765 = state_12727__$1;
(statearr_12742_12765[(2)] = inst_12723);

(statearr_12742_12765[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_12728 === (8))){
var inst_12704 = (state_12727[(7)]);
var inst_12714 = cljs.core.next.call(null,inst_12704);
var inst_12704__$1 = inst_12714;
var state_12727__$1 = (function (){var statearr_12743 = state_12727;
(statearr_12743[(7)] = inst_12704__$1);

return statearr_12743;
})();
var statearr_12744_12766 = state_12727__$1;
(statearr_12744_12766[(2)] = null);

(statearr_12744_12766[(1)] = (2));


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
}
}
}
}
}
});})(c__6803__auto__))
;
return ((function (switch__6741__auto__,c__6803__auto__){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_12748 = [null,null,null,null,null,null,null,null];
(statearr_12748[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_12748[(1)] = (1));

return statearr_12748;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_12727){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_12727);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e12749){if((e12749 instanceof Object)){
var ex__6745__auto__ = e12749;
var statearr_12750_12767 = state_12727;
(statearr_12750_12767[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_12727);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e12749;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__12768 = state_12727;
state_12727 = G__12768;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_12727){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_12727);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto__))
})();
var state__6805__auto__ = (function (){var statearr_12751 = f__6804__auto__.call(null);
(statearr_12751[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto__);

return statearr_12751;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto__))
);

return c__6803__auto__;
});

cljs.core.async.onto_chan.cljs$lang$maxFixedArity = 3;
/**
 * Creates and returns a channel which contains the contents of coll,
 * closing when exhausted.
 */
cljs.core.async.to_chan = (function cljs$core$async$to_chan(coll){
var ch = cljs.core.async.chan.call(null,cljs.core.bounded_count.call(null,(100),coll));
cljs.core.async.onto_chan.call(null,ch,coll);

return ch;
});

cljs.core.async.Mux = (function (){var obj12770 = {};
return obj12770;
})();

cljs.core.async.muxch_STAR_ = (function cljs$core$async$muxch_STAR_(_){
if((function (){var and__4309__auto__ = _;
if(and__4309__auto__){
return _.cljs$core$async$Mux$muxch_STAR_$arity$1;
} else {
return and__4309__auto__;
}
})()){
return _.cljs$core$async$Mux$muxch_STAR_$arity$1(_);
} else {
var x__4957__auto__ = (((_ == null))?null:_);
return (function (){var or__4321__auto__ = (cljs.core.async.muxch_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.muxch_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mux.muxch*",_);
}
}
})().call(null,_);
}
});


cljs.core.async.Mult = (function (){var obj12772 = {};
return obj12772;
})();

cljs.core.async.tap_STAR_ = (function cljs$core$async$tap_STAR_(m,ch,close_QMARK_){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mult$tap_STAR_$arity$3;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mult$tap_STAR_$arity$3(m,ch,close_QMARK_);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.tap_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.tap_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mult.tap*",m);
}
}
})().call(null,m,ch,close_QMARK_);
}
});

cljs.core.async.untap_STAR_ = (function cljs$core$async$untap_STAR_(m,ch){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mult$untap_STAR_$arity$2;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mult$untap_STAR_$arity$2(m,ch);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.untap_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.untap_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mult.untap*",m);
}
}
})().call(null,m,ch);
}
});

cljs.core.async.untap_all_STAR_ = (function cljs$core$async$untap_all_STAR_(m){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mult$untap_all_STAR_$arity$1;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mult$untap_all_STAR_$arity$1(m);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.untap_all_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.untap_all_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mult.untap-all*",m);
}
}
})().call(null,m);
}
});

/**
 * Creates and returns a mult(iple) of the supplied channel. Channels
 * containing copies of the channel can be created with 'tap', and
 * detached with 'untap'.
 * 
 * Each item is distributed to all taps in parallel and synchronously,
 * i.e. each tap must accept before the next item is distributed. Use
 * buffering/windowing to prevent slow taps from holding up the mult.
 * 
 * Items received when there are no taps get dropped.
 * 
 * If a tap puts to a closed channel, it will be removed from the mult.
 */
cljs.core.async.mult = (function cljs$core$async$mult(ch){
var cs = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var m = (function (){
if(typeof cljs.core.async.t12994 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t12994 = (function (mult,ch,cs,meta12995){
this.mult = mult;
this.ch = ch;
this.cs = cs;
this.meta12995 = meta12995;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t12994.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (cs){
return (function (_12996,meta12995__$1){
var self__ = this;
var _12996__$1 = this;
return (new cljs.core.async.t12994(self__.mult,self__.ch,self__.cs,meta12995__$1));
});})(cs))
;

cljs.core.async.t12994.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (cs){
return (function (_12996){
var self__ = this;
var _12996__$1 = this;
return self__.meta12995;
});})(cs))
;

cljs.core.async.t12994.prototype.cljs$core$async$Mux$ = true;

cljs.core.async.t12994.prototype.cljs$core$async$Mux$muxch_STAR_$arity$1 = ((function (cs){
return (function (_){
var self__ = this;
var ___$1 = this;
return self__.ch;
});})(cs))
;

cljs.core.async.t12994.prototype.cljs$core$async$Mult$ = true;

cljs.core.async.t12994.prototype.cljs$core$async$Mult$tap_STAR_$arity$3 = ((function (cs){
return (function (_,ch__$1,close_QMARK_){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.assoc,ch__$1,close_QMARK_);

return null;
});})(cs))
;

cljs.core.async.t12994.prototype.cljs$core$async$Mult$untap_STAR_$arity$2 = ((function (cs){
return (function (_,ch__$1){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.dissoc,ch__$1);

return null;
});})(cs))
;

cljs.core.async.t12994.prototype.cljs$core$async$Mult$untap_all_STAR_$arity$1 = ((function (cs){
return (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.reset_BANG_.call(null,self__.cs,cljs.core.PersistentArrayMap.EMPTY);

return null;
});})(cs))
;

cljs.core.async.t12994.getBasis = ((function (cs){
return (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"mult","mult",-1187640995,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"cs","cs",-117024463,null),new cljs.core.Symbol(null,"meta12995","meta12995",-326401199,null)], null);
});})(cs))
;

cljs.core.async.t12994.cljs$lang$type = true;

cljs.core.async.t12994.cljs$lang$ctorStr = "cljs.core.async/t12994";

cljs.core.async.t12994.cljs$lang$ctorPrWriter = ((function (cs){
return (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t12994");
});})(cs))
;

cljs.core.async.__GT_t12994 = ((function (cs){
return (function cljs$core$async$mult_$___GT_t12994(mult__$1,ch__$1,cs__$1,meta12995){
return (new cljs.core.async.t12994(mult__$1,ch__$1,cs__$1,meta12995));
});})(cs))
;

}

return (new cljs.core.async.t12994(cljs$core$async$mult,ch,cs,cljs.core.PersistentArrayMap.EMPTY));
})()
;
var dchan = cljs.core.async.chan.call(null,(1));
var dctr = cljs.core.atom.call(null,null);
var done = ((function (cs,m,dchan,dctr){
return (function (_){
if((cljs.core.swap_BANG_.call(null,dctr,cljs.core.dec) === (0))){
return cljs.core.async.put_BANG_.call(null,dchan,true);
} else {
return null;
}
});})(cs,m,dchan,dctr))
;
var c__6803__auto___13215 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___13215,cs,m,dchan,dctr,done){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___13215,cs,m,dchan,dctr,done){
return (function (state_13127){
var state_val_13128 = (state_13127[(1)]);
if((state_val_13128 === (7))){
var inst_13123 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13129_13216 = state_13127__$1;
(statearr_13129_13216[(2)] = inst_13123);

(statearr_13129_13216[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (20))){
var inst_13028 = (state_13127[(7)]);
var inst_13038 = cljs.core.first.call(null,inst_13028);
var inst_13039 = cljs.core.nth.call(null,inst_13038,(0),null);
var inst_13040 = cljs.core.nth.call(null,inst_13038,(1),null);
var state_13127__$1 = (function (){var statearr_13130 = state_13127;
(statearr_13130[(8)] = inst_13039);

return statearr_13130;
})();
if(cljs.core.truth_(inst_13040)){
var statearr_13131_13217 = state_13127__$1;
(statearr_13131_13217[(1)] = (22));

} else {
var statearr_13132_13218 = state_13127__$1;
(statearr_13132_13218[(1)] = (23));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (27))){
var inst_13075 = (state_13127[(9)]);
var inst_13068 = (state_13127[(10)]);
var inst_12999 = (state_13127[(11)]);
var inst_13070 = (state_13127[(12)]);
var inst_13075__$1 = cljs.core._nth.call(null,inst_13068,inst_13070);
var inst_13076 = cljs.core.async.put_BANG_.call(null,inst_13075__$1,inst_12999,done);
var state_13127__$1 = (function (){var statearr_13133 = state_13127;
(statearr_13133[(9)] = inst_13075__$1);

return statearr_13133;
})();
if(cljs.core.truth_(inst_13076)){
var statearr_13134_13219 = state_13127__$1;
(statearr_13134_13219[(1)] = (30));

} else {
var statearr_13135_13220 = state_13127__$1;
(statearr_13135_13220[(1)] = (31));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (1))){
var state_13127__$1 = state_13127;
var statearr_13136_13221 = state_13127__$1;
(statearr_13136_13221[(2)] = null);

(statearr_13136_13221[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (24))){
var inst_13028 = (state_13127[(7)]);
var inst_13045 = (state_13127[(2)]);
var inst_13046 = cljs.core.next.call(null,inst_13028);
var inst_13008 = inst_13046;
var inst_13009 = null;
var inst_13010 = (0);
var inst_13011 = (0);
var state_13127__$1 = (function (){var statearr_13137 = state_13127;
(statearr_13137[(13)] = inst_13009);

(statearr_13137[(14)] = inst_13011);

(statearr_13137[(15)] = inst_13008);

(statearr_13137[(16)] = inst_13010);

(statearr_13137[(17)] = inst_13045);

return statearr_13137;
})();
var statearr_13138_13222 = state_13127__$1;
(statearr_13138_13222[(2)] = null);

(statearr_13138_13222[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (39))){
var state_13127__$1 = state_13127;
var statearr_13142_13223 = state_13127__$1;
(statearr_13142_13223[(2)] = null);

(statearr_13142_13223[(1)] = (41));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (4))){
var inst_12999 = (state_13127[(11)]);
var inst_12999__$1 = (state_13127[(2)]);
var inst_13000 = (inst_12999__$1 == null);
var state_13127__$1 = (function (){var statearr_13143 = state_13127;
(statearr_13143[(11)] = inst_12999__$1);

return statearr_13143;
})();
if(cljs.core.truth_(inst_13000)){
var statearr_13144_13224 = state_13127__$1;
(statearr_13144_13224[(1)] = (5));

} else {
var statearr_13145_13225 = state_13127__$1;
(statearr_13145_13225[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (15))){
var inst_13009 = (state_13127[(13)]);
var inst_13011 = (state_13127[(14)]);
var inst_13008 = (state_13127[(15)]);
var inst_13010 = (state_13127[(16)]);
var inst_13024 = (state_13127[(2)]);
var inst_13025 = (inst_13011 + (1));
var tmp13139 = inst_13009;
var tmp13140 = inst_13008;
var tmp13141 = inst_13010;
var inst_13008__$1 = tmp13140;
var inst_13009__$1 = tmp13139;
var inst_13010__$1 = tmp13141;
var inst_13011__$1 = inst_13025;
var state_13127__$1 = (function (){var statearr_13146 = state_13127;
(statearr_13146[(13)] = inst_13009__$1);

(statearr_13146[(14)] = inst_13011__$1);

(statearr_13146[(15)] = inst_13008__$1);

(statearr_13146[(16)] = inst_13010__$1);

(statearr_13146[(18)] = inst_13024);

return statearr_13146;
})();
var statearr_13147_13226 = state_13127__$1;
(statearr_13147_13226[(2)] = null);

(statearr_13147_13226[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (21))){
var inst_13049 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13151_13227 = state_13127__$1;
(statearr_13151_13227[(2)] = inst_13049);

(statearr_13151_13227[(1)] = (18));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (31))){
var inst_13075 = (state_13127[(9)]);
var inst_13079 = done.call(null,null);
var inst_13080 = cljs.core.async.untap_STAR_.call(null,m,inst_13075);
var state_13127__$1 = (function (){var statearr_13152 = state_13127;
(statearr_13152[(19)] = inst_13079);

return statearr_13152;
})();
var statearr_13153_13228 = state_13127__$1;
(statearr_13153_13228[(2)] = inst_13080);

(statearr_13153_13228[(1)] = (32));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (32))){
var inst_13068 = (state_13127[(10)]);
var inst_13067 = (state_13127[(20)]);
var inst_13070 = (state_13127[(12)]);
var inst_13069 = (state_13127[(21)]);
var inst_13082 = (state_13127[(2)]);
var inst_13083 = (inst_13070 + (1));
var tmp13148 = inst_13068;
var tmp13149 = inst_13067;
var tmp13150 = inst_13069;
var inst_13067__$1 = tmp13149;
var inst_13068__$1 = tmp13148;
var inst_13069__$1 = tmp13150;
var inst_13070__$1 = inst_13083;
var state_13127__$1 = (function (){var statearr_13154 = state_13127;
(statearr_13154[(22)] = inst_13082);

(statearr_13154[(10)] = inst_13068__$1);

(statearr_13154[(20)] = inst_13067__$1);

(statearr_13154[(12)] = inst_13070__$1);

(statearr_13154[(21)] = inst_13069__$1);

return statearr_13154;
})();
var statearr_13155_13229 = state_13127__$1;
(statearr_13155_13229[(2)] = null);

(statearr_13155_13229[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (40))){
var inst_13095 = (state_13127[(23)]);
var inst_13099 = done.call(null,null);
var inst_13100 = cljs.core.async.untap_STAR_.call(null,m,inst_13095);
var state_13127__$1 = (function (){var statearr_13156 = state_13127;
(statearr_13156[(24)] = inst_13099);

return statearr_13156;
})();
var statearr_13157_13230 = state_13127__$1;
(statearr_13157_13230[(2)] = inst_13100);

(statearr_13157_13230[(1)] = (41));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (33))){
var inst_13086 = (state_13127[(25)]);
var inst_13088 = cljs.core.chunked_seq_QMARK_.call(null,inst_13086);
var state_13127__$1 = state_13127;
if(inst_13088){
var statearr_13158_13231 = state_13127__$1;
(statearr_13158_13231[(1)] = (36));

} else {
var statearr_13159_13232 = state_13127__$1;
(statearr_13159_13232[(1)] = (37));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (13))){
var inst_13018 = (state_13127[(26)]);
var inst_13021 = cljs.core.async.close_BANG_.call(null,inst_13018);
var state_13127__$1 = state_13127;
var statearr_13160_13233 = state_13127__$1;
(statearr_13160_13233[(2)] = inst_13021);

(statearr_13160_13233[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (22))){
var inst_13039 = (state_13127[(8)]);
var inst_13042 = cljs.core.async.close_BANG_.call(null,inst_13039);
var state_13127__$1 = state_13127;
var statearr_13161_13234 = state_13127__$1;
(statearr_13161_13234[(2)] = inst_13042);

(statearr_13161_13234[(1)] = (24));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (36))){
var inst_13086 = (state_13127[(25)]);
var inst_13090 = cljs.core.chunk_first.call(null,inst_13086);
var inst_13091 = cljs.core.chunk_rest.call(null,inst_13086);
var inst_13092 = cljs.core.count.call(null,inst_13090);
var inst_13067 = inst_13091;
var inst_13068 = inst_13090;
var inst_13069 = inst_13092;
var inst_13070 = (0);
var state_13127__$1 = (function (){var statearr_13162 = state_13127;
(statearr_13162[(10)] = inst_13068);

(statearr_13162[(20)] = inst_13067);

(statearr_13162[(12)] = inst_13070);

(statearr_13162[(21)] = inst_13069);

return statearr_13162;
})();
var statearr_13163_13235 = state_13127__$1;
(statearr_13163_13235[(2)] = null);

(statearr_13163_13235[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (41))){
var inst_13086 = (state_13127[(25)]);
var inst_13102 = (state_13127[(2)]);
var inst_13103 = cljs.core.next.call(null,inst_13086);
var inst_13067 = inst_13103;
var inst_13068 = null;
var inst_13069 = (0);
var inst_13070 = (0);
var state_13127__$1 = (function (){var statearr_13164 = state_13127;
(statearr_13164[(10)] = inst_13068);

(statearr_13164[(20)] = inst_13067);

(statearr_13164[(12)] = inst_13070);

(statearr_13164[(27)] = inst_13102);

(statearr_13164[(21)] = inst_13069);

return statearr_13164;
})();
var statearr_13165_13236 = state_13127__$1;
(statearr_13165_13236[(2)] = null);

(statearr_13165_13236[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (43))){
var state_13127__$1 = state_13127;
var statearr_13166_13237 = state_13127__$1;
(statearr_13166_13237[(2)] = null);

(statearr_13166_13237[(1)] = (44));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (29))){
var inst_13111 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13167_13238 = state_13127__$1;
(statearr_13167_13238[(2)] = inst_13111);

(statearr_13167_13238[(1)] = (26));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (44))){
var inst_13120 = (state_13127[(2)]);
var state_13127__$1 = (function (){var statearr_13168 = state_13127;
(statearr_13168[(28)] = inst_13120);

return statearr_13168;
})();
var statearr_13169_13239 = state_13127__$1;
(statearr_13169_13239[(2)] = null);

(statearr_13169_13239[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (6))){
var inst_13059 = (state_13127[(29)]);
var inst_13058 = cljs.core.deref.call(null,cs);
var inst_13059__$1 = cljs.core.keys.call(null,inst_13058);
var inst_13060 = cljs.core.count.call(null,inst_13059__$1);
var inst_13061 = cljs.core.reset_BANG_.call(null,dctr,inst_13060);
var inst_13066 = cljs.core.seq.call(null,inst_13059__$1);
var inst_13067 = inst_13066;
var inst_13068 = null;
var inst_13069 = (0);
var inst_13070 = (0);
var state_13127__$1 = (function (){var statearr_13170 = state_13127;
(statearr_13170[(29)] = inst_13059__$1);

(statearr_13170[(10)] = inst_13068);

(statearr_13170[(20)] = inst_13067);

(statearr_13170[(30)] = inst_13061);

(statearr_13170[(12)] = inst_13070);

(statearr_13170[(21)] = inst_13069);

return statearr_13170;
})();
var statearr_13171_13240 = state_13127__$1;
(statearr_13171_13240[(2)] = null);

(statearr_13171_13240[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (28))){
var inst_13086 = (state_13127[(25)]);
var inst_13067 = (state_13127[(20)]);
var inst_13086__$1 = cljs.core.seq.call(null,inst_13067);
var state_13127__$1 = (function (){var statearr_13172 = state_13127;
(statearr_13172[(25)] = inst_13086__$1);

return statearr_13172;
})();
if(inst_13086__$1){
var statearr_13173_13241 = state_13127__$1;
(statearr_13173_13241[(1)] = (33));

} else {
var statearr_13174_13242 = state_13127__$1;
(statearr_13174_13242[(1)] = (34));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (25))){
var inst_13070 = (state_13127[(12)]);
var inst_13069 = (state_13127[(21)]);
var inst_13072 = (inst_13070 < inst_13069);
var inst_13073 = inst_13072;
var state_13127__$1 = state_13127;
if(cljs.core.truth_(inst_13073)){
var statearr_13175_13243 = state_13127__$1;
(statearr_13175_13243[(1)] = (27));

} else {
var statearr_13176_13244 = state_13127__$1;
(statearr_13176_13244[(1)] = (28));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (34))){
var state_13127__$1 = state_13127;
var statearr_13177_13245 = state_13127__$1;
(statearr_13177_13245[(2)] = null);

(statearr_13177_13245[(1)] = (35));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (17))){
var state_13127__$1 = state_13127;
var statearr_13178_13246 = state_13127__$1;
(statearr_13178_13246[(2)] = null);

(statearr_13178_13246[(1)] = (18));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (3))){
var inst_13125 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_13127__$1,inst_13125);
} else {
if((state_val_13128 === (12))){
var inst_13054 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13179_13247 = state_13127__$1;
(statearr_13179_13247[(2)] = inst_13054);

(statearr_13179_13247[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (2))){
var state_13127__$1 = state_13127;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_13127__$1,(4),ch);
} else {
if((state_val_13128 === (23))){
var state_13127__$1 = state_13127;
var statearr_13180_13248 = state_13127__$1;
(statearr_13180_13248[(2)] = null);

(statearr_13180_13248[(1)] = (24));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (35))){
var inst_13109 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13181_13249 = state_13127__$1;
(statearr_13181_13249[(2)] = inst_13109);

(statearr_13181_13249[(1)] = (29));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (19))){
var inst_13028 = (state_13127[(7)]);
var inst_13032 = cljs.core.chunk_first.call(null,inst_13028);
var inst_13033 = cljs.core.chunk_rest.call(null,inst_13028);
var inst_13034 = cljs.core.count.call(null,inst_13032);
var inst_13008 = inst_13033;
var inst_13009 = inst_13032;
var inst_13010 = inst_13034;
var inst_13011 = (0);
var state_13127__$1 = (function (){var statearr_13182 = state_13127;
(statearr_13182[(13)] = inst_13009);

(statearr_13182[(14)] = inst_13011);

(statearr_13182[(15)] = inst_13008);

(statearr_13182[(16)] = inst_13010);

return statearr_13182;
})();
var statearr_13183_13250 = state_13127__$1;
(statearr_13183_13250[(2)] = null);

(statearr_13183_13250[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (11))){
var inst_13028 = (state_13127[(7)]);
var inst_13008 = (state_13127[(15)]);
var inst_13028__$1 = cljs.core.seq.call(null,inst_13008);
var state_13127__$1 = (function (){var statearr_13184 = state_13127;
(statearr_13184[(7)] = inst_13028__$1);

return statearr_13184;
})();
if(inst_13028__$1){
var statearr_13185_13251 = state_13127__$1;
(statearr_13185_13251[(1)] = (16));

} else {
var statearr_13186_13252 = state_13127__$1;
(statearr_13186_13252[(1)] = (17));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (9))){
var inst_13056 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13187_13253 = state_13127__$1;
(statearr_13187_13253[(2)] = inst_13056);

(statearr_13187_13253[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (5))){
var inst_13006 = cljs.core.deref.call(null,cs);
var inst_13007 = cljs.core.seq.call(null,inst_13006);
var inst_13008 = inst_13007;
var inst_13009 = null;
var inst_13010 = (0);
var inst_13011 = (0);
var state_13127__$1 = (function (){var statearr_13188 = state_13127;
(statearr_13188[(13)] = inst_13009);

(statearr_13188[(14)] = inst_13011);

(statearr_13188[(15)] = inst_13008);

(statearr_13188[(16)] = inst_13010);

return statearr_13188;
})();
var statearr_13189_13254 = state_13127__$1;
(statearr_13189_13254[(2)] = null);

(statearr_13189_13254[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (14))){
var state_13127__$1 = state_13127;
var statearr_13190_13255 = state_13127__$1;
(statearr_13190_13255[(2)] = null);

(statearr_13190_13255[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (45))){
var inst_13117 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13191_13256 = state_13127__$1;
(statearr_13191_13256[(2)] = inst_13117);

(statearr_13191_13256[(1)] = (44));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (26))){
var inst_13059 = (state_13127[(29)]);
var inst_13113 = (state_13127[(2)]);
var inst_13114 = cljs.core.seq.call(null,inst_13059);
var state_13127__$1 = (function (){var statearr_13192 = state_13127;
(statearr_13192[(31)] = inst_13113);

return statearr_13192;
})();
if(inst_13114){
var statearr_13193_13257 = state_13127__$1;
(statearr_13193_13257[(1)] = (42));

} else {
var statearr_13194_13258 = state_13127__$1;
(statearr_13194_13258[(1)] = (43));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (16))){
var inst_13028 = (state_13127[(7)]);
var inst_13030 = cljs.core.chunked_seq_QMARK_.call(null,inst_13028);
var state_13127__$1 = state_13127;
if(inst_13030){
var statearr_13195_13259 = state_13127__$1;
(statearr_13195_13259[(1)] = (19));

} else {
var statearr_13196_13260 = state_13127__$1;
(statearr_13196_13260[(1)] = (20));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (38))){
var inst_13106 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13197_13261 = state_13127__$1;
(statearr_13197_13261[(2)] = inst_13106);

(statearr_13197_13261[(1)] = (35));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (30))){
var state_13127__$1 = state_13127;
var statearr_13198_13262 = state_13127__$1;
(statearr_13198_13262[(2)] = null);

(statearr_13198_13262[(1)] = (32));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (10))){
var inst_13009 = (state_13127[(13)]);
var inst_13011 = (state_13127[(14)]);
var inst_13017 = cljs.core._nth.call(null,inst_13009,inst_13011);
var inst_13018 = cljs.core.nth.call(null,inst_13017,(0),null);
var inst_13019 = cljs.core.nth.call(null,inst_13017,(1),null);
var state_13127__$1 = (function (){var statearr_13199 = state_13127;
(statearr_13199[(26)] = inst_13018);

return statearr_13199;
})();
if(cljs.core.truth_(inst_13019)){
var statearr_13200_13263 = state_13127__$1;
(statearr_13200_13263[(1)] = (13));

} else {
var statearr_13201_13264 = state_13127__$1;
(statearr_13201_13264[(1)] = (14));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (18))){
var inst_13052 = (state_13127[(2)]);
var state_13127__$1 = state_13127;
var statearr_13202_13265 = state_13127__$1;
(statearr_13202_13265[(2)] = inst_13052);

(statearr_13202_13265[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (42))){
var state_13127__$1 = state_13127;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_13127__$1,(45),dchan);
} else {
if((state_val_13128 === (37))){
var inst_13086 = (state_13127[(25)]);
var inst_13095 = (state_13127[(23)]);
var inst_12999 = (state_13127[(11)]);
var inst_13095__$1 = cljs.core.first.call(null,inst_13086);
var inst_13096 = cljs.core.async.put_BANG_.call(null,inst_13095__$1,inst_12999,done);
var state_13127__$1 = (function (){var statearr_13203 = state_13127;
(statearr_13203[(23)] = inst_13095__$1);

return statearr_13203;
})();
if(cljs.core.truth_(inst_13096)){
var statearr_13204_13266 = state_13127__$1;
(statearr_13204_13266[(1)] = (39));

} else {
var statearr_13205_13267 = state_13127__$1;
(statearr_13205_13267[(1)] = (40));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13128 === (8))){
var inst_13011 = (state_13127[(14)]);
var inst_13010 = (state_13127[(16)]);
var inst_13013 = (inst_13011 < inst_13010);
var inst_13014 = inst_13013;
var state_13127__$1 = state_13127;
if(cljs.core.truth_(inst_13014)){
var statearr_13206_13268 = state_13127__$1;
(statearr_13206_13268[(1)] = (10));

} else {
var statearr_13207_13269 = state_13127__$1;
(statearr_13207_13269[(1)] = (11));

}

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
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__6803__auto___13215,cs,m,dchan,dctr,done))
;
return ((function (switch__6741__auto__,c__6803__auto___13215,cs,m,dchan,dctr,done){
return (function() {
var cljs$core$async$mult_$_state_machine__6742__auto__ = null;
var cljs$core$async$mult_$_state_machine__6742__auto____0 = (function (){
var statearr_13211 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_13211[(0)] = cljs$core$async$mult_$_state_machine__6742__auto__);

(statearr_13211[(1)] = (1));

return statearr_13211;
});
var cljs$core$async$mult_$_state_machine__6742__auto____1 = (function (state_13127){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_13127);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e13212){if((e13212 instanceof Object)){
var ex__6745__auto__ = e13212;
var statearr_13213_13270 = state_13127;
(statearr_13213_13270[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13127);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e13212;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__13271 = state_13127;
state_13127 = G__13271;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$mult_$_state_machine__6742__auto__ = function(state_13127){
switch(arguments.length){
case 0:
return cljs$core$async$mult_$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$mult_$_state_machine__6742__auto____1.call(this,state_13127);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$mult_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$mult_$_state_machine__6742__auto____0;
cljs$core$async$mult_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$mult_$_state_machine__6742__auto____1;
return cljs$core$async$mult_$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___13215,cs,m,dchan,dctr,done))
})();
var state__6805__auto__ = (function (){var statearr_13214 = f__6804__auto__.call(null);
(statearr_13214[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___13215);

return statearr_13214;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___13215,cs,m,dchan,dctr,done))
);


return m;
});
/**
 * Copies the mult source onto the supplied channel.
 * 
 * By default the channel will be closed when the source closes,
 * but can be determined by the close? parameter.
 */
cljs.core.async.tap = (function cljs$core$async$tap(){
var G__13273 = arguments.length;
switch (G__13273) {
case 2:
return cljs.core.async.tap.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.tap.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.tap.cljs$core$IFn$_invoke$arity$2 = (function (mult,ch){
return cljs.core.async.tap.call(null,mult,ch,true);
});

cljs.core.async.tap.cljs$core$IFn$_invoke$arity$3 = (function (mult,ch,close_QMARK_){
cljs.core.async.tap_STAR_.call(null,mult,ch,close_QMARK_);

return ch;
});

cljs.core.async.tap.cljs$lang$maxFixedArity = 3;
/**
 * Disconnects a target channel from a mult
 */
cljs.core.async.untap = (function cljs$core$async$untap(mult,ch){
return cljs.core.async.untap_STAR_.call(null,mult,ch);
});
/**
 * Disconnects all target channels from a mult
 */
cljs.core.async.untap_all = (function cljs$core$async$untap_all(mult){
return cljs.core.async.untap_all_STAR_.call(null,mult);
});

cljs.core.async.Mix = (function (){var obj13276 = {};
return obj13276;
})();

cljs.core.async.admix_STAR_ = (function cljs$core$async$admix_STAR_(m,ch){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mix$admix_STAR_$arity$2;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mix$admix_STAR_$arity$2(m,ch);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.admix_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.admix_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.admix*",m);
}
}
})().call(null,m,ch);
}
});

cljs.core.async.unmix_STAR_ = (function cljs$core$async$unmix_STAR_(m,ch){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mix$unmix_STAR_$arity$2;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mix$unmix_STAR_$arity$2(m,ch);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.unmix_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.unmix_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.unmix*",m);
}
}
})().call(null,m,ch);
}
});

cljs.core.async.unmix_all_STAR_ = (function cljs$core$async$unmix_all_STAR_(m){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mix$unmix_all_STAR_$arity$1;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mix$unmix_all_STAR_$arity$1(m);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.unmix_all_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.unmix_all_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.unmix-all*",m);
}
}
})().call(null,m);
}
});

cljs.core.async.toggle_STAR_ = (function cljs$core$async$toggle_STAR_(m,state_map){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mix$toggle_STAR_$arity$2;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mix$toggle_STAR_$arity$2(m,state_map);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.toggle_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.toggle_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.toggle*",m);
}
}
})().call(null,m,state_map);
}
});

cljs.core.async.solo_mode_STAR_ = (function cljs$core$async$solo_mode_STAR_(m,mode){
if((function (){var and__4309__auto__ = m;
if(and__4309__auto__){
return m.cljs$core$async$Mix$solo_mode_STAR_$arity$2;
} else {
return and__4309__auto__;
}
})()){
return m.cljs$core$async$Mix$solo_mode_STAR_$arity$2(m,mode);
} else {
var x__4957__auto__ = (((m == null))?null:m);
return (function (){var or__4321__auto__ = (cljs.core.async.solo_mode_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.solo_mode_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.solo-mode*",m);
}
}
})().call(null,m,mode);
}
});

cljs.core.async.ioc_alts_BANG_ = (function cljs$core$async$ioc_alts_BANG_(){
var argseq__5361__auto__ = ((((3) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(3)),(0))):null);
return cljs.core.async.ioc_alts_BANG_.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),argseq__5361__auto__);
});

cljs.core.async.ioc_alts_BANG_.cljs$core$IFn$_invoke$arity$variadic = (function (state,cont_block,ports,p__13281){
var map__13282 = p__13281;
var map__13282__$1 = ((cljs.core.seq_QMARK_.call(null,map__13282))?cljs.core.apply.call(null,cljs.core.hash_map,map__13282):map__13282);
var opts = map__13282__$1;
var statearr_13283_13286 = state;
(statearr_13283_13286[cljs.core.async.impl.ioc_helpers.STATE_IDX] = cont_block);


var temp__4425__auto__ = cljs.core.async.do_alts.call(null,((function (map__13282,map__13282__$1,opts){
return (function (val){
var statearr_13284_13287 = state;
(statearr_13284_13287[cljs.core.async.impl.ioc_helpers.VALUE_IDX] = val);


return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state);
});})(map__13282,map__13282__$1,opts))
,ports,opts);
if(cljs.core.truth_(temp__4425__auto__)){
var cb = temp__4425__auto__;
var statearr_13285_13288 = state;
(statearr_13285_13288[cljs.core.async.impl.ioc_helpers.VALUE_IDX] = cljs.core.deref.call(null,cb));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
});

cljs.core.async.ioc_alts_BANG_.cljs$lang$maxFixedArity = (3);

cljs.core.async.ioc_alts_BANG_.cljs$lang$applyTo = (function (seq13277){
var G__13278 = cljs.core.first.call(null,seq13277);
var seq13277__$1 = cljs.core.next.call(null,seq13277);
var G__13279 = cljs.core.first.call(null,seq13277__$1);
var seq13277__$2 = cljs.core.next.call(null,seq13277__$1);
var G__13280 = cljs.core.first.call(null,seq13277__$2);
var seq13277__$3 = cljs.core.next.call(null,seq13277__$2);
return cljs.core.async.ioc_alts_BANG_.cljs$core$IFn$_invoke$arity$variadic(G__13278,G__13279,G__13280,seq13277__$3);
});
/**
 * Creates and returns a mix of one or more input channels which will
 * be put on the supplied out channel. Input sources can be added to
 * the mix with 'admix', and removed with 'unmix'. A mix supports
 * soloing, muting and pausing multiple inputs atomically using
 * 'toggle', and can solo using either muting or pausing as determined
 * by 'solo-mode'.
 * 
 * Each channel can have zero or more boolean modes set via 'toggle':
 * 
 * :solo - when true, only this (ond other soloed) channel(s) will appear
 * in the mix output channel. :mute and :pause states of soloed
 * channels are ignored. If solo-mode is :mute, non-soloed
 * channels are muted, if :pause, non-soloed channels are
 * paused.
 * 
 * :mute - muted channels will have their contents consumed but not included in the mix
 * :pause - paused channels will not have their contents consumed (and thus also not included in the mix)
 */
cljs.core.async.mix = (function cljs$core$async$mix(out){
var cs = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var solo_modes = new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"pause","pause",-2095325672),null,new cljs.core.Keyword(null,"mute","mute",1151223646),null], null), null);
var attrs = cljs.core.conj.call(null,solo_modes,new cljs.core.Keyword(null,"solo","solo",-316350075));
var solo_mode = cljs.core.atom.call(null,new cljs.core.Keyword(null,"mute","mute",1151223646));
var change = cljs.core.async.chan.call(null);
var changed = ((function (cs,solo_modes,attrs,solo_mode,change){
return (function (){
return cljs.core.async.put_BANG_.call(null,change,true);
});})(cs,solo_modes,attrs,solo_mode,change))
;
var pick = ((function (cs,solo_modes,attrs,solo_mode,change,changed){
return (function (attr,chs){
return cljs.core.reduce_kv.call(null,((function (cs,solo_modes,attrs,solo_mode,change,changed){
return (function (ret,c,v){
if(cljs.core.truth_(attr.call(null,v))){
return cljs.core.conj.call(null,ret,c);
} else {
return ret;
}
});})(cs,solo_modes,attrs,solo_mode,change,changed))
,cljs.core.PersistentHashSet.EMPTY,chs);
});})(cs,solo_modes,attrs,solo_mode,change,changed))
;
var calc_state = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick){
return (function (){
var chs = cljs.core.deref.call(null,cs);
var mode = cljs.core.deref.call(null,solo_mode);
var solos = pick.call(null,new cljs.core.Keyword(null,"solo","solo",-316350075),chs);
var pauses = pick.call(null,new cljs.core.Keyword(null,"pause","pause",-2095325672),chs);
return new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"solos","solos",1441458643),solos,new cljs.core.Keyword(null,"mutes","mutes",1068806309),pick.call(null,new cljs.core.Keyword(null,"mute","mute",1151223646),chs),new cljs.core.Keyword(null,"reads","reads",-1215067361),cljs.core.conj.call(null,(((cljs.core._EQ_.call(null,mode,new cljs.core.Keyword(null,"pause","pause",-2095325672))) && (!(cljs.core.empty_QMARK_.call(null,solos))))?cljs.core.vec.call(null,solos):cljs.core.vec.call(null,cljs.core.remove.call(null,pauses,cljs.core.keys.call(null,chs)))),change)], null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick))
;
var m = (function (){
if(typeof cljs.core.async.t13408 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t13408 = (function (change,mix,solo_mode,pick,cs,calc_state,out,changed,solo_modes,attrs,meta13409){
this.change = change;
this.mix = mix;
this.solo_mode = solo_mode;
this.pick = pick;
this.cs = cs;
this.calc_state = calc_state;
this.out = out;
this.changed = changed;
this.solo_modes = solo_modes;
this.attrs = attrs;
this.meta13409 = meta13409;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t13408.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_13410,meta13409__$1){
var self__ = this;
var _13410__$1 = this;
return (new cljs.core.async.t13408(self__.change,self__.mix,self__.solo_mode,self__.pick,self__.cs,self__.calc_state,self__.out,self__.changed,self__.solo_modes,self__.attrs,meta13409__$1));
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_13410){
var self__ = this;
var _13410__$1 = this;
return self__.meta13409;
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.prototype.cljs$core$async$Mux$ = true;

cljs.core.async.t13408.prototype.cljs$core$async$Mux$muxch_STAR_$arity$1 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_){
var self__ = this;
var ___$1 = this;
return self__.out;
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.prototype.cljs$core$async$Mix$ = true;

cljs.core.async.t13408.prototype.cljs$core$async$Mix$admix_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,ch){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.assoc,ch,cljs.core.PersistentArrayMap.EMPTY);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.prototype.cljs$core$async$Mix$unmix_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,ch){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.dissoc,ch);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.prototype.cljs$core$async$Mix$unmix_all_STAR_$arity$1 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.reset_BANG_.call(null,self__.cs,cljs.core.PersistentArrayMap.EMPTY);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.prototype.cljs$core$async$Mix$toggle_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,state_map){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.partial.call(null,cljs.core.merge_with,cljs.core.merge),state_map);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.prototype.cljs$core$async$Mix$solo_mode_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,mode){
var self__ = this;
var ___$1 = this;
if(cljs.core.truth_(self__.solo_modes.call(null,mode))){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str([cljs.core.str("mode must be one of: "),cljs.core.str(self__.solo_modes)].join('')),cljs.core.str("\n"),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol(null,"solo-modes","solo-modes",882180540,null),new cljs.core.Symbol(null,"mode","mode",-2000032078,null))))].join('')));
}

cljs.core.reset_BANG_.call(null,self__.solo_mode,mode);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.getBasis = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (){
return new cljs.core.PersistentVector(null, 11, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"change","change",477485025,null),new cljs.core.Symbol(null,"mix","mix",2121373763,null),new cljs.core.Symbol(null,"solo-mode","solo-mode",2031788074,null),new cljs.core.Symbol(null,"pick","pick",1300068175,null),new cljs.core.Symbol(null,"cs","cs",-117024463,null),new cljs.core.Symbol(null,"calc-state","calc-state",-349968968,null),new cljs.core.Symbol(null,"out","out",729986010,null),new cljs.core.Symbol(null,"changed","changed",-2083710852,null),new cljs.core.Symbol(null,"solo-modes","solo-modes",882180540,null),new cljs.core.Symbol(null,"attrs","attrs",-450137186,null),new cljs.core.Symbol(null,"meta13409","meta13409",-436323013,null)], null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t13408.cljs$lang$type = true;

cljs.core.async.t13408.cljs$lang$ctorStr = "cljs.core.async/t13408";

cljs.core.async.t13408.cljs$lang$ctorPrWriter = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t13408");
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.__GT_t13408 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function cljs$core$async$mix_$___GT_t13408(change__$1,mix__$1,solo_mode__$1,pick__$1,cs__$1,calc_state__$1,out__$1,changed__$1,solo_modes__$1,attrs__$1,meta13409){
return (new cljs.core.async.t13408(change__$1,mix__$1,solo_mode__$1,pick__$1,cs__$1,calc_state__$1,out__$1,changed__$1,solo_modes__$1,attrs__$1,meta13409));
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

}

return (new cljs.core.async.t13408(change,cljs$core$async$mix,solo_mode,pick,cs,calc_state,out,changed,solo_modes,attrs,cljs.core.PersistentArrayMap.EMPTY));
})()
;
var c__6803__auto___13527 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___13527,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___13527,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m){
return (function (state_13480){
var state_val_13481 = (state_13480[(1)]);
if((state_val_13481 === (7))){
var inst_13424 = (state_13480[(7)]);
var inst_13429 = cljs.core.apply.call(null,cljs.core.hash_map,inst_13424);
var state_13480__$1 = state_13480;
var statearr_13482_13528 = state_13480__$1;
(statearr_13482_13528[(2)] = inst_13429);

(statearr_13482_13528[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (20))){
var inst_13439 = (state_13480[(8)]);
var state_13480__$1 = state_13480;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_13480__$1,(23),out,inst_13439);
} else {
if((state_val_13481 === (1))){
var inst_13414 = (state_13480[(9)]);
var inst_13414__$1 = calc_state.call(null);
var inst_13415 = cljs.core.seq_QMARK_.call(null,inst_13414__$1);
var state_13480__$1 = (function (){var statearr_13483 = state_13480;
(statearr_13483[(9)] = inst_13414__$1);

return statearr_13483;
})();
if(inst_13415){
var statearr_13484_13529 = state_13480__$1;
(statearr_13484_13529[(1)] = (2));

} else {
var statearr_13485_13530 = state_13480__$1;
(statearr_13485_13530[(1)] = (3));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (24))){
var inst_13432 = (state_13480[(10)]);
var inst_13424 = inst_13432;
var state_13480__$1 = (function (){var statearr_13486 = state_13480;
(statearr_13486[(7)] = inst_13424);

return statearr_13486;
})();
var statearr_13487_13531 = state_13480__$1;
(statearr_13487_13531[(2)] = null);

(statearr_13487_13531[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (4))){
var inst_13414 = (state_13480[(9)]);
var inst_13420 = (state_13480[(2)]);
var inst_13421 = cljs.core.get.call(null,inst_13420,new cljs.core.Keyword(null,"solos","solos",1441458643));
var inst_13422 = cljs.core.get.call(null,inst_13420,new cljs.core.Keyword(null,"mutes","mutes",1068806309));
var inst_13423 = cljs.core.get.call(null,inst_13420,new cljs.core.Keyword(null,"reads","reads",-1215067361));
var inst_13424 = inst_13414;
var state_13480__$1 = (function (){var statearr_13488 = state_13480;
(statearr_13488[(11)] = inst_13423);

(statearr_13488[(7)] = inst_13424);

(statearr_13488[(12)] = inst_13421);

(statearr_13488[(13)] = inst_13422);

return statearr_13488;
})();
var statearr_13489_13532 = state_13480__$1;
(statearr_13489_13532[(2)] = null);

(statearr_13489_13532[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (15))){
var state_13480__$1 = state_13480;
var statearr_13490_13533 = state_13480__$1;
(statearr_13490_13533[(2)] = null);

(statearr_13490_13533[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (21))){
var inst_13432 = (state_13480[(10)]);
var inst_13424 = inst_13432;
var state_13480__$1 = (function (){var statearr_13491 = state_13480;
(statearr_13491[(7)] = inst_13424);

return statearr_13491;
})();
var statearr_13492_13534 = state_13480__$1;
(statearr_13492_13534[(2)] = null);

(statearr_13492_13534[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (13))){
var inst_13476 = (state_13480[(2)]);
var state_13480__$1 = state_13480;
var statearr_13493_13535 = state_13480__$1;
(statearr_13493_13535[(2)] = inst_13476);

(statearr_13493_13535[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (22))){
var inst_13474 = (state_13480[(2)]);
var state_13480__$1 = state_13480;
var statearr_13494_13536 = state_13480__$1;
(statearr_13494_13536[(2)] = inst_13474);

(statearr_13494_13536[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (6))){
var inst_13478 = (state_13480[(2)]);
var state_13480__$1 = state_13480;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_13480__$1,inst_13478);
} else {
if((state_val_13481 === (25))){
var state_13480__$1 = state_13480;
var statearr_13495_13537 = state_13480__$1;
(statearr_13495_13537[(2)] = null);

(statearr_13495_13537[(1)] = (26));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (17))){
var inst_13454 = (state_13480[(14)]);
var state_13480__$1 = state_13480;
var statearr_13496_13538 = state_13480__$1;
(statearr_13496_13538[(2)] = inst_13454);

(statearr_13496_13538[(1)] = (19));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (3))){
var inst_13414 = (state_13480[(9)]);
var state_13480__$1 = state_13480;
var statearr_13497_13539 = state_13480__$1;
(statearr_13497_13539[(2)] = inst_13414);

(statearr_13497_13539[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (12))){
var inst_13454 = (state_13480[(14)]);
var inst_13440 = (state_13480[(15)]);
var inst_13433 = (state_13480[(16)]);
var inst_13454__$1 = inst_13433.call(null,inst_13440);
var state_13480__$1 = (function (){var statearr_13498 = state_13480;
(statearr_13498[(14)] = inst_13454__$1);

return statearr_13498;
})();
if(cljs.core.truth_(inst_13454__$1)){
var statearr_13499_13540 = state_13480__$1;
(statearr_13499_13540[(1)] = (17));

} else {
var statearr_13500_13541 = state_13480__$1;
(statearr_13500_13541[(1)] = (18));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (2))){
var inst_13414 = (state_13480[(9)]);
var inst_13417 = cljs.core.apply.call(null,cljs.core.hash_map,inst_13414);
var state_13480__$1 = state_13480;
var statearr_13501_13542 = state_13480__$1;
(statearr_13501_13542[(2)] = inst_13417);

(statearr_13501_13542[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (23))){
var inst_13465 = (state_13480[(2)]);
var state_13480__$1 = state_13480;
if(cljs.core.truth_(inst_13465)){
var statearr_13502_13543 = state_13480__$1;
(statearr_13502_13543[(1)] = (24));

} else {
var statearr_13503_13544 = state_13480__$1;
(statearr_13503_13544[(1)] = (25));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (19))){
var inst_13462 = (state_13480[(2)]);
var state_13480__$1 = state_13480;
if(cljs.core.truth_(inst_13462)){
var statearr_13504_13545 = state_13480__$1;
(statearr_13504_13545[(1)] = (20));

} else {
var statearr_13505_13546 = state_13480__$1;
(statearr_13505_13546[(1)] = (21));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (11))){
var inst_13439 = (state_13480[(8)]);
var inst_13445 = (inst_13439 == null);
var state_13480__$1 = state_13480;
if(cljs.core.truth_(inst_13445)){
var statearr_13506_13547 = state_13480__$1;
(statearr_13506_13547[(1)] = (14));

} else {
var statearr_13507_13548 = state_13480__$1;
(statearr_13507_13548[(1)] = (15));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (9))){
var inst_13432 = (state_13480[(10)]);
var inst_13432__$1 = (state_13480[(2)]);
var inst_13433 = cljs.core.get.call(null,inst_13432__$1,new cljs.core.Keyword(null,"solos","solos",1441458643));
var inst_13434 = cljs.core.get.call(null,inst_13432__$1,new cljs.core.Keyword(null,"mutes","mutes",1068806309));
var inst_13435 = cljs.core.get.call(null,inst_13432__$1,new cljs.core.Keyword(null,"reads","reads",-1215067361));
var state_13480__$1 = (function (){var statearr_13508 = state_13480;
(statearr_13508[(10)] = inst_13432__$1);

(statearr_13508[(17)] = inst_13434);

(statearr_13508[(16)] = inst_13433);

return statearr_13508;
})();
return cljs.core.async.ioc_alts_BANG_.call(null,state_13480__$1,(10),inst_13435);
} else {
if((state_val_13481 === (5))){
var inst_13424 = (state_13480[(7)]);
var inst_13427 = cljs.core.seq_QMARK_.call(null,inst_13424);
var state_13480__$1 = state_13480;
if(inst_13427){
var statearr_13509_13549 = state_13480__$1;
(statearr_13509_13549[(1)] = (7));

} else {
var statearr_13510_13550 = state_13480__$1;
(statearr_13510_13550[(1)] = (8));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (14))){
var inst_13440 = (state_13480[(15)]);
var inst_13447 = cljs.core.swap_BANG_.call(null,cs,cljs.core.dissoc,inst_13440);
var state_13480__$1 = state_13480;
var statearr_13511_13551 = state_13480__$1;
(statearr_13511_13551[(2)] = inst_13447);

(statearr_13511_13551[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (26))){
var inst_13470 = (state_13480[(2)]);
var state_13480__$1 = state_13480;
var statearr_13512_13552 = state_13480__$1;
(statearr_13512_13552[(2)] = inst_13470);

(statearr_13512_13552[(1)] = (22));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (16))){
var inst_13450 = (state_13480[(2)]);
var inst_13451 = calc_state.call(null);
var inst_13424 = inst_13451;
var state_13480__$1 = (function (){var statearr_13513 = state_13480;
(statearr_13513[(18)] = inst_13450);

(statearr_13513[(7)] = inst_13424);

return statearr_13513;
})();
var statearr_13514_13553 = state_13480__$1;
(statearr_13514_13553[(2)] = null);

(statearr_13514_13553[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (10))){
var inst_13440 = (state_13480[(15)]);
var inst_13439 = (state_13480[(8)]);
var inst_13438 = (state_13480[(2)]);
var inst_13439__$1 = cljs.core.nth.call(null,inst_13438,(0),null);
var inst_13440__$1 = cljs.core.nth.call(null,inst_13438,(1),null);
var inst_13441 = (inst_13439__$1 == null);
var inst_13442 = cljs.core._EQ_.call(null,inst_13440__$1,change);
var inst_13443 = (inst_13441) || (inst_13442);
var state_13480__$1 = (function (){var statearr_13515 = state_13480;
(statearr_13515[(15)] = inst_13440__$1);

(statearr_13515[(8)] = inst_13439__$1);

return statearr_13515;
})();
if(cljs.core.truth_(inst_13443)){
var statearr_13516_13554 = state_13480__$1;
(statearr_13516_13554[(1)] = (11));

} else {
var statearr_13517_13555 = state_13480__$1;
(statearr_13517_13555[(1)] = (12));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (18))){
var inst_13434 = (state_13480[(17)]);
var inst_13440 = (state_13480[(15)]);
var inst_13433 = (state_13480[(16)]);
var inst_13457 = cljs.core.empty_QMARK_.call(null,inst_13433);
var inst_13458 = inst_13434.call(null,inst_13440);
var inst_13459 = cljs.core.not.call(null,inst_13458);
var inst_13460 = (inst_13457) && (inst_13459);
var state_13480__$1 = state_13480;
var statearr_13518_13556 = state_13480__$1;
(statearr_13518_13556[(2)] = inst_13460);

(statearr_13518_13556[(1)] = (19));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13481 === (8))){
var inst_13424 = (state_13480[(7)]);
var state_13480__$1 = state_13480;
var statearr_13519_13557 = state_13480__$1;
(statearr_13519_13557[(2)] = inst_13424);

(statearr_13519_13557[(1)] = (9));


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
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__6803__auto___13527,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m))
;
return ((function (switch__6741__auto__,c__6803__auto___13527,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m){
return (function() {
var cljs$core$async$mix_$_state_machine__6742__auto__ = null;
var cljs$core$async$mix_$_state_machine__6742__auto____0 = (function (){
var statearr_13523 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_13523[(0)] = cljs$core$async$mix_$_state_machine__6742__auto__);

(statearr_13523[(1)] = (1));

return statearr_13523;
});
var cljs$core$async$mix_$_state_machine__6742__auto____1 = (function (state_13480){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_13480);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e13524){if((e13524 instanceof Object)){
var ex__6745__auto__ = e13524;
var statearr_13525_13558 = state_13480;
(statearr_13525_13558[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13480);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e13524;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__13559 = state_13480;
state_13480 = G__13559;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$mix_$_state_machine__6742__auto__ = function(state_13480){
switch(arguments.length){
case 0:
return cljs$core$async$mix_$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$mix_$_state_machine__6742__auto____1.call(this,state_13480);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$mix_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$mix_$_state_machine__6742__auto____0;
cljs$core$async$mix_$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$mix_$_state_machine__6742__auto____1;
return cljs$core$async$mix_$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___13527,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m))
})();
var state__6805__auto__ = (function (){var statearr_13526 = f__6804__auto__.call(null);
(statearr_13526[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___13527);

return statearr_13526;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___13527,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m))
);


return m;
});
/**
 * Adds ch as an input to the mix
 */
cljs.core.async.admix = (function cljs$core$async$admix(mix,ch){
return cljs.core.async.admix_STAR_.call(null,mix,ch);
});
/**
 * Removes ch as an input to the mix
 */
cljs.core.async.unmix = (function cljs$core$async$unmix(mix,ch){
return cljs.core.async.unmix_STAR_.call(null,mix,ch);
});
/**
 * removes all inputs from the mix
 */
cljs.core.async.unmix_all = (function cljs$core$async$unmix_all(mix){
return cljs.core.async.unmix_all_STAR_.call(null,mix);
});
/**
 * Atomically sets the state(s) of one or more channels in a mix. The
 * state map is a map of channels -> channel-state-map. A
 * channel-state-map is a map of attrs -> boolean, where attr is one or
 * more of :mute, :pause or :solo. Any states supplied are merged with
 * the current state.
 * 
 * Note that channels can be added to a mix via toggle, which can be
 * used to add channels in a particular (e.g. paused) state.
 */
cljs.core.async.toggle = (function cljs$core$async$toggle(mix,state_map){
return cljs.core.async.toggle_STAR_.call(null,mix,state_map);
});
/**
 * Sets the solo mode of the mix. mode must be one of :mute or :pause
 */
cljs.core.async.solo_mode = (function cljs$core$async$solo_mode(mix,mode){
return cljs.core.async.solo_mode_STAR_.call(null,mix,mode);
});

cljs.core.async.Pub = (function (){var obj13561 = {};
return obj13561;
})();

cljs.core.async.sub_STAR_ = (function cljs$core$async$sub_STAR_(p,v,ch,close_QMARK_){
if((function (){var and__4309__auto__ = p;
if(and__4309__auto__){
return p.cljs$core$async$Pub$sub_STAR_$arity$4;
} else {
return and__4309__auto__;
}
})()){
return p.cljs$core$async$Pub$sub_STAR_$arity$4(p,v,ch,close_QMARK_);
} else {
var x__4957__auto__ = (((p == null))?null:p);
return (function (){var or__4321__auto__ = (cljs.core.async.sub_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.sub_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.sub*",p);
}
}
})().call(null,p,v,ch,close_QMARK_);
}
});

cljs.core.async.unsub_STAR_ = (function cljs$core$async$unsub_STAR_(p,v,ch){
if((function (){var and__4309__auto__ = p;
if(and__4309__auto__){
return p.cljs$core$async$Pub$unsub_STAR_$arity$3;
} else {
return and__4309__auto__;
}
})()){
return p.cljs$core$async$Pub$unsub_STAR_$arity$3(p,v,ch);
} else {
var x__4957__auto__ = (((p == null))?null:p);
return (function (){var or__4321__auto__ = (cljs.core.async.unsub_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.unsub_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.unsub*",p);
}
}
})().call(null,p,v,ch);
}
});

cljs.core.async.unsub_all_STAR_ = (function cljs$core$async$unsub_all_STAR_(){
var G__13563 = arguments.length;
switch (G__13563) {
case 1:
return cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$1 = (function (p){
if((function (){var and__4309__auto__ = p;
if(and__4309__auto__){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$1;
} else {
return and__4309__auto__;
}
})()){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$1(p);
} else {
var x__4957__auto__ = (((p == null))?null:p);
return (function (){var or__4321__auto__ = (cljs.core.async.unsub_all_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.unsub_all_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.unsub-all*",p);
}
}
})().call(null,p);
}
});

cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$2 = (function (p,v){
if((function (){var and__4309__auto__ = p;
if(and__4309__auto__){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$2;
} else {
return and__4309__auto__;
}
})()){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$2(p,v);
} else {
var x__4957__auto__ = (((p == null))?null:p);
return (function (){var or__4321__auto__ = (cljs.core.async.unsub_all_STAR_[goog.typeOf(x__4957__auto__)]);
if(or__4321__auto__){
return or__4321__auto__;
} else {
var or__4321__auto____$1 = (cljs.core.async.unsub_all_STAR_["_"]);
if(or__4321__auto____$1){
return or__4321__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.unsub-all*",p);
}
}
})().call(null,p,v);
}
});

cljs.core.async.unsub_all_STAR_.cljs$lang$maxFixedArity = 2;

/**
 * Creates and returns a pub(lication) of the supplied channel,
 * partitioned into topics by the topic-fn. topic-fn will be applied to
 * each value on the channel and the result will determine the 'topic'
 * on which that value will be put. Channels can be subscribed to
 * receive copies of topics using 'sub', and unsubscribed using
 * 'unsub'. Each topic will be handled by an internal mult on a
 * dedicated channel. By default these internal channels are
 * unbuffered, but a buf-fn can be supplied which, given a topic,
 * creates a buffer with desired properties.
 * 
 * Each item is distributed to all subs in parallel and synchronously,
 * i.e. each sub must accept before the next item is distributed. Use
 * buffering/windowing to prevent slow subs from holding up the pub.
 * 
 * Items received when there are no matching subs get dropped.
 * 
 * Note that if buf-fns are used then each topic is handled
 * asynchronously, i.e. if a channel is subscribed to more than one
 * topic it should not expect them to be interleaved identically with
 * the source.
 */
cljs.core.async.pub = (function cljs$core$async$pub(){
var G__13567 = arguments.length;
switch (G__13567) {
case 2:
return cljs.core.async.pub.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.pub.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pub.cljs$core$IFn$_invoke$arity$2 = (function (ch,topic_fn){
return cljs.core.async.pub.call(null,ch,topic_fn,cljs.core.constantly.call(null,null));
});

cljs.core.async.pub.cljs$core$IFn$_invoke$arity$3 = (function (ch,topic_fn,buf_fn){
var mults = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var ensure_mult = ((function (mults){
return (function (topic){
var or__4321__auto__ = cljs.core.get.call(null,cljs.core.deref.call(null,mults),topic);
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return cljs.core.get.call(null,cljs.core.swap_BANG_.call(null,mults,((function (or__4321__auto__,mults){
return (function (p1__13565_SHARP_){
if(cljs.core.truth_(p1__13565_SHARP_.call(null,topic))){
return p1__13565_SHARP_;
} else {
return cljs.core.assoc.call(null,p1__13565_SHARP_,topic,cljs.core.async.mult.call(null,cljs.core.async.chan.call(null,buf_fn.call(null,topic))));
}
});})(or__4321__auto__,mults))
),topic);
}
});})(mults))
;
var p = (function (){
if(typeof cljs.core.async.t13568 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t13568 = (function (ch,topic_fn,buf_fn,mults,ensure_mult,meta13569){
this.ch = ch;
this.topic_fn = topic_fn;
this.buf_fn = buf_fn;
this.mults = mults;
this.ensure_mult = ensure_mult;
this.meta13569 = meta13569;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t13568.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (mults,ensure_mult){
return (function (_13570,meta13569__$1){
var self__ = this;
var _13570__$1 = this;
return (new cljs.core.async.t13568(self__.ch,self__.topic_fn,self__.buf_fn,self__.mults,self__.ensure_mult,meta13569__$1));
});})(mults,ensure_mult))
;

cljs.core.async.t13568.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (mults,ensure_mult){
return (function (_13570){
var self__ = this;
var _13570__$1 = this;
return self__.meta13569;
});})(mults,ensure_mult))
;

cljs.core.async.t13568.prototype.cljs$core$async$Mux$ = true;

cljs.core.async.t13568.prototype.cljs$core$async$Mux$muxch_STAR_$arity$1 = ((function (mults,ensure_mult){
return (function (_){
var self__ = this;
var ___$1 = this;
return self__.ch;
});})(mults,ensure_mult))
;

cljs.core.async.t13568.prototype.cljs$core$async$Pub$ = true;

cljs.core.async.t13568.prototype.cljs$core$async$Pub$sub_STAR_$arity$4 = ((function (mults,ensure_mult){
return (function (p,topic,ch__$1,close_QMARK_){
var self__ = this;
var p__$1 = this;
var m = self__.ensure_mult.call(null,topic);
return cljs.core.async.tap.call(null,m,ch__$1,close_QMARK_);
});})(mults,ensure_mult))
;

cljs.core.async.t13568.prototype.cljs$core$async$Pub$unsub_STAR_$arity$3 = ((function (mults,ensure_mult){
return (function (p,topic,ch__$1){
var self__ = this;
var p__$1 = this;
var temp__4425__auto__ = cljs.core.get.call(null,cljs.core.deref.call(null,self__.mults),topic);
if(cljs.core.truth_(temp__4425__auto__)){
var m = temp__4425__auto__;
return cljs.core.async.untap.call(null,m,ch__$1);
} else {
return null;
}
});})(mults,ensure_mult))
;

cljs.core.async.t13568.prototype.cljs$core$async$Pub$unsub_all_STAR_$arity$1 = ((function (mults,ensure_mult){
return (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.reset_BANG_.call(null,self__.mults,cljs.core.PersistentArrayMap.EMPTY);
});})(mults,ensure_mult))
;

cljs.core.async.t13568.prototype.cljs$core$async$Pub$unsub_all_STAR_$arity$2 = ((function (mults,ensure_mult){
return (function (_,topic){
var self__ = this;
var ___$1 = this;
return cljs.core.swap_BANG_.call(null,self__.mults,cljs.core.dissoc,topic);
});})(mults,ensure_mult))
;

cljs.core.async.t13568.getBasis = ((function (mults,ensure_mult){
return (function (){
return new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"topic-fn","topic-fn",-862449736,null),new cljs.core.Symbol(null,"buf-fn","buf-fn",-1200281591,null),new cljs.core.Symbol(null,"mults","mults",-461114485,null),new cljs.core.Symbol(null,"ensure-mult","ensure-mult",1796584816,null),new cljs.core.Symbol(null,"meta13569","meta13569",1453517339,null)], null);
});})(mults,ensure_mult))
;

cljs.core.async.t13568.cljs$lang$type = true;

cljs.core.async.t13568.cljs$lang$ctorStr = "cljs.core.async/t13568";

cljs.core.async.t13568.cljs$lang$ctorPrWriter = ((function (mults,ensure_mult){
return (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t13568");
});})(mults,ensure_mult))
;

cljs.core.async.__GT_t13568 = ((function (mults,ensure_mult){
return (function cljs$core$async$__GT_t13568(ch__$1,topic_fn__$1,buf_fn__$1,mults__$1,ensure_mult__$1,meta13569){
return (new cljs.core.async.t13568(ch__$1,topic_fn__$1,buf_fn__$1,mults__$1,ensure_mult__$1,meta13569));
});})(mults,ensure_mult))
;

}

return (new cljs.core.async.t13568(ch,topic_fn,buf_fn,mults,ensure_mult,cljs.core.PersistentArrayMap.EMPTY));
})()
;
var c__6803__auto___13691 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___13691,mults,ensure_mult,p){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___13691,mults,ensure_mult,p){
return (function (state_13642){
var state_val_13643 = (state_13642[(1)]);
if((state_val_13643 === (7))){
var inst_13638 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
var statearr_13644_13692 = state_13642__$1;
(statearr_13644_13692[(2)] = inst_13638);

(statearr_13644_13692[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (20))){
var state_13642__$1 = state_13642;
var statearr_13645_13693 = state_13642__$1;
(statearr_13645_13693[(2)] = null);

(statearr_13645_13693[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (1))){
var state_13642__$1 = state_13642;
var statearr_13646_13694 = state_13642__$1;
(statearr_13646_13694[(2)] = null);

(statearr_13646_13694[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (24))){
var inst_13621 = (state_13642[(7)]);
var inst_13630 = cljs.core.swap_BANG_.call(null,mults,cljs.core.dissoc,inst_13621);
var state_13642__$1 = state_13642;
var statearr_13647_13695 = state_13642__$1;
(statearr_13647_13695[(2)] = inst_13630);

(statearr_13647_13695[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (4))){
var inst_13573 = (state_13642[(8)]);
var inst_13573__$1 = (state_13642[(2)]);
var inst_13574 = (inst_13573__$1 == null);
var state_13642__$1 = (function (){var statearr_13648 = state_13642;
(statearr_13648[(8)] = inst_13573__$1);

return statearr_13648;
})();
if(cljs.core.truth_(inst_13574)){
var statearr_13649_13696 = state_13642__$1;
(statearr_13649_13696[(1)] = (5));

} else {
var statearr_13650_13697 = state_13642__$1;
(statearr_13650_13697[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (15))){
var inst_13615 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
var statearr_13651_13698 = state_13642__$1;
(statearr_13651_13698[(2)] = inst_13615);

(statearr_13651_13698[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (21))){
var inst_13635 = (state_13642[(2)]);
var state_13642__$1 = (function (){var statearr_13652 = state_13642;
(statearr_13652[(9)] = inst_13635);

return statearr_13652;
})();
var statearr_13653_13699 = state_13642__$1;
(statearr_13653_13699[(2)] = null);

(statearr_13653_13699[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (13))){
var inst_13597 = (state_13642[(10)]);
var inst_13599 = cljs.core.chunked_seq_QMARK_.call(null,inst_13597);
var state_13642__$1 = state_13642;
if(inst_13599){
var statearr_13654_13700 = state_13642__$1;
(statearr_13654_13700[(1)] = (16));

} else {
var statearr_13655_13701 = state_13642__$1;
(statearr_13655_13701[(1)] = (17));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (22))){
var inst_13627 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
if(cljs.core.truth_(inst_13627)){
var statearr_13656_13702 = state_13642__$1;
(statearr_13656_13702[(1)] = (23));

} else {
var statearr_13657_13703 = state_13642__$1;
(statearr_13657_13703[(1)] = (24));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (6))){
var inst_13623 = (state_13642[(11)]);
var inst_13573 = (state_13642[(8)]);
var inst_13621 = (state_13642[(7)]);
var inst_13621__$1 = topic_fn.call(null,inst_13573);
var inst_13622 = cljs.core.deref.call(null,mults);
var inst_13623__$1 = cljs.core.get.call(null,inst_13622,inst_13621__$1);
var state_13642__$1 = (function (){var statearr_13658 = state_13642;
(statearr_13658[(11)] = inst_13623__$1);

(statearr_13658[(7)] = inst_13621__$1);

return statearr_13658;
})();
if(cljs.core.truth_(inst_13623__$1)){
var statearr_13659_13704 = state_13642__$1;
(statearr_13659_13704[(1)] = (19));

} else {
var statearr_13660_13705 = state_13642__$1;
(statearr_13660_13705[(1)] = (20));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (25))){
var inst_13632 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
var statearr_13661_13706 = state_13642__$1;
(statearr_13661_13706[(2)] = inst_13632);

(statearr_13661_13706[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (17))){
var inst_13597 = (state_13642[(10)]);
var inst_13606 = cljs.core.first.call(null,inst_13597);
var inst_13607 = cljs.core.async.muxch_STAR_.call(null,inst_13606);
var inst_13608 = cljs.core.async.close_BANG_.call(null,inst_13607);
var inst_13609 = cljs.core.next.call(null,inst_13597);
var inst_13583 = inst_13609;
var inst_13584 = null;
var inst_13585 = (0);
var inst_13586 = (0);
var state_13642__$1 = (function (){var statearr_13662 = state_13642;
(statearr_13662[(12)] = inst_13584);

(statearr_13662[(13)] = inst_13583);

(statearr_13662[(14)] = inst_13585);

(statearr_13662[(15)] = inst_13586);

(statearr_13662[(16)] = inst_13608);

return statearr_13662;
})();
var statearr_13663_13707 = state_13642__$1;
(statearr_13663_13707[(2)] = null);

(statearr_13663_13707[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (3))){
var inst_13640 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_13642__$1,inst_13640);
} else {
if((state_val_13643 === (12))){
var inst_13617 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
var statearr_13664_13708 = state_13642__$1;
(statearr_13664_13708[(2)] = inst_13617);

(statearr_13664_13708[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (2))){
var state_13642__$1 = state_13642;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_13642__$1,(4),ch);
} else {
if((state_val_13643 === (23))){
var state_13642__$1 = state_13642;
var statearr_13665_13709 = state_13642__$1;
(statearr_13665_13709[(2)] = null);

(statearr_13665_13709[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (19))){
var inst_13623 = (state_13642[(11)]);
var inst_13573 = (state_13642[(8)]);
var inst_13625 = cljs.core.async.muxch_STAR_.call(null,inst_13623);
var state_13642__$1 = state_13642;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_13642__$1,(22),inst_13625,inst_13573);
} else {
if((state_val_13643 === (11))){
var inst_13597 = (state_13642[(10)]);
var inst_13583 = (state_13642[(13)]);
var inst_13597__$1 = cljs.core.seq.call(null,inst_13583);
var state_13642__$1 = (function (){var statearr_13666 = state_13642;
(statearr_13666[(10)] = inst_13597__$1);

return statearr_13666;
})();
if(inst_13597__$1){
var statearr_13667_13710 = state_13642__$1;
(statearr_13667_13710[(1)] = (13));

} else {
var statearr_13668_13711 = state_13642__$1;
(statearr_13668_13711[(1)] = (14));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (9))){
var inst_13619 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
var statearr_13669_13712 = state_13642__$1;
(statearr_13669_13712[(2)] = inst_13619);

(statearr_13669_13712[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (5))){
var inst_13580 = cljs.core.deref.call(null,mults);
var inst_13581 = cljs.core.vals.call(null,inst_13580);
var inst_13582 = cljs.core.seq.call(null,inst_13581);
var inst_13583 = inst_13582;
var inst_13584 = null;
var inst_13585 = (0);
var inst_13586 = (0);
var state_13642__$1 = (function (){var statearr_13670 = state_13642;
(statearr_13670[(12)] = inst_13584);

(statearr_13670[(13)] = inst_13583);

(statearr_13670[(14)] = inst_13585);

(statearr_13670[(15)] = inst_13586);

return statearr_13670;
})();
var statearr_13671_13713 = state_13642__$1;
(statearr_13671_13713[(2)] = null);

(statearr_13671_13713[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (14))){
var state_13642__$1 = state_13642;
var statearr_13675_13714 = state_13642__$1;
(statearr_13675_13714[(2)] = null);

(statearr_13675_13714[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (16))){
var inst_13597 = (state_13642[(10)]);
var inst_13601 = cljs.core.chunk_first.call(null,inst_13597);
var inst_13602 = cljs.core.chunk_rest.call(null,inst_13597);
var inst_13603 = cljs.core.count.call(null,inst_13601);
var inst_13583 = inst_13602;
var inst_13584 = inst_13601;
var inst_13585 = inst_13603;
var inst_13586 = (0);
var state_13642__$1 = (function (){var statearr_13676 = state_13642;
(statearr_13676[(12)] = inst_13584);

(statearr_13676[(13)] = inst_13583);

(statearr_13676[(14)] = inst_13585);

(statearr_13676[(15)] = inst_13586);

return statearr_13676;
})();
var statearr_13677_13715 = state_13642__$1;
(statearr_13677_13715[(2)] = null);

(statearr_13677_13715[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (10))){
var inst_13584 = (state_13642[(12)]);
var inst_13583 = (state_13642[(13)]);
var inst_13585 = (state_13642[(14)]);
var inst_13586 = (state_13642[(15)]);
var inst_13591 = cljs.core._nth.call(null,inst_13584,inst_13586);
var inst_13592 = cljs.core.async.muxch_STAR_.call(null,inst_13591);
var inst_13593 = cljs.core.async.close_BANG_.call(null,inst_13592);
var inst_13594 = (inst_13586 + (1));
var tmp13672 = inst_13584;
var tmp13673 = inst_13583;
var tmp13674 = inst_13585;
var inst_13583__$1 = tmp13673;
var inst_13584__$1 = tmp13672;
var inst_13585__$1 = tmp13674;
var inst_13586__$1 = inst_13594;
var state_13642__$1 = (function (){var statearr_13678 = state_13642;
(statearr_13678[(12)] = inst_13584__$1);

(statearr_13678[(17)] = inst_13593);

(statearr_13678[(13)] = inst_13583__$1);

(statearr_13678[(14)] = inst_13585__$1);

(statearr_13678[(15)] = inst_13586__$1);

return statearr_13678;
})();
var statearr_13679_13716 = state_13642__$1;
(statearr_13679_13716[(2)] = null);

(statearr_13679_13716[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (18))){
var inst_13612 = (state_13642[(2)]);
var state_13642__$1 = state_13642;
var statearr_13680_13717 = state_13642__$1;
(statearr_13680_13717[(2)] = inst_13612);

(statearr_13680_13717[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13643 === (8))){
var inst_13585 = (state_13642[(14)]);
var inst_13586 = (state_13642[(15)]);
var inst_13588 = (inst_13586 < inst_13585);
var inst_13589 = inst_13588;
var state_13642__$1 = state_13642;
if(cljs.core.truth_(inst_13589)){
var statearr_13681_13718 = state_13642__$1;
(statearr_13681_13718[(1)] = (10));

} else {
var statearr_13682_13719 = state_13642__$1;
(statearr_13682_13719[(1)] = (11));

}

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
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__6803__auto___13691,mults,ensure_mult,p))
;
return ((function (switch__6741__auto__,c__6803__auto___13691,mults,ensure_mult,p){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_13686 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_13686[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_13686[(1)] = (1));

return statearr_13686;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_13642){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_13642);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e13687){if((e13687 instanceof Object)){
var ex__6745__auto__ = e13687;
var statearr_13688_13720 = state_13642;
(statearr_13688_13720[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13642);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e13687;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__13721 = state_13642;
state_13642 = G__13721;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_13642){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_13642);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___13691,mults,ensure_mult,p))
})();
var state__6805__auto__ = (function (){var statearr_13689 = f__6804__auto__.call(null);
(statearr_13689[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___13691);

return statearr_13689;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___13691,mults,ensure_mult,p))
);


return p;
});

cljs.core.async.pub.cljs$lang$maxFixedArity = 3;
/**
 * Subscribes a channel to a topic of a pub.
 * 
 * By default the channel will be closed when the source closes,
 * but can be determined by the close? parameter.
 */
cljs.core.async.sub = (function cljs$core$async$sub(){
var G__13723 = arguments.length;
switch (G__13723) {
case 3:
return cljs.core.async.sub.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return cljs.core.async.sub.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.sub.cljs$core$IFn$_invoke$arity$3 = (function (p,topic,ch){
return cljs.core.async.sub.call(null,p,topic,ch,true);
});

cljs.core.async.sub.cljs$core$IFn$_invoke$arity$4 = (function (p,topic,ch,close_QMARK_){
return cljs.core.async.sub_STAR_.call(null,p,topic,ch,close_QMARK_);
});

cljs.core.async.sub.cljs$lang$maxFixedArity = 4;
/**
 * Unsubscribes a channel from a topic of a pub
 */
cljs.core.async.unsub = (function cljs$core$async$unsub(p,topic,ch){
return cljs.core.async.unsub_STAR_.call(null,p,topic,ch);
});
/**
 * Unsubscribes all channels from a pub, or a topic of a pub
 */
cljs.core.async.unsub_all = (function cljs$core$async$unsub_all(){
var G__13726 = arguments.length;
switch (G__13726) {
case 1:
return cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$1 = (function (p){
return cljs.core.async.unsub_all_STAR_.call(null,p);
});

cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$2 = (function (p,topic){
return cljs.core.async.unsub_all_STAR_.call(null,p,topic);
});

cljs.core.async.unsub_all.cljs$lang$maxFixedArity = 2;
/**
 * Takes a function and a collection of source channels, and returns a
 * channel which contains the values produced by applying f to the set
 * of first items taken from each source channel, followed by applying
 * f to the set of second items from each channel, until any one of the
 * channels is closed, at which point the output channel will be
 * closed. The returned channel will be unbuffered by default, or a
 * buf-or-n can be supplied
 */
cljs.core.async.map = (function cljs$core$async$map(){
var G__13729 = arguments.length;
switch (G__13729) {
case 2:
return cljs.core.async.map.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.map.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.map.cljs$core$IFn$_invoke$arity$2 = (function (f,chs){
return cljs.core.async.map.call(null,f,chs,null);
});

cljs.core.async.map.cljs$core$IFn$_invoke$arity$3 = (function (f,chs,buf_or_n){
var chs__$1 = cljs.core.vec.call(null,chs);
var out = cljs.core.async.chan.call(null,buf_or_n);
var cnt = cljs.core.count.call(null,chs__$1);
var rets = cljs.core.object_array.call(null,cnt);
var dchan = cljs.core.async.chan.call(null,(1));
var dctr = cljs.core.atom.call(null,null);
var done = cljs.core.mapv.call(null,((function (chs__$1,out,cnt,rets,dchan,dctr){
return (function (i){
return ((function (chs__$1,out,cnt,rets,dchan,dctr){
return (function (ret){
(rets[i] = ret);

if((cljs.core.swap_BANG_.call(null,dctr,cljs.core.dec) === (0))){
return cljs.core.async.put_BANG_.call(null,dchan,rets.slice((0)));
} else {
return null;
}
});
;})(chs__$1,out,cnt,rets,dchan,dctr))
});})(chs__$1,out,cnt,rets,dchan,dctr))
,cljs.core.range.call(null,cnt));
var c__6803__auto___13799 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___13799,chs__$1,out,cnt,rets,dchan,dctr,done){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___13799,chs__$1,out,cnt,rets,dchan,dctr,done){
return (function (state_13768){
var state_val_13769 = (state_13768[(1)]);
if((state_val_13769 === (7))){
var state_13768__$1 = state_13768;
var statearr_13770_13800 = state_13768__$1;
(statearr_13770_13800[(2)] = null);

(statearr_13770_13800[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (1))){
var state_13768__$1 = state_13768;
var statearr_13771_13801 = state_13768__$1;
(statearr_13771_13801[(2)] = null);

(statearr_13771_13801[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (4))){
var inst_13732 = (state_13768[(7)]);
var inst_13734 = (inst_13732 < cnt);
var state_13768__$1 = state_13768;
if(cljs.core.truth_(inst_13734)){
var statearr_13772_13802 = state_13768__$1;
(statearr_13772_13802[(1)] = (6));

} else {
var statearr_13773_13803 = state_13768__$1;
(statearr_13773_13803[(1)] = (7));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (15))){
var inst_13764 = (state_13768[(2)]);
var state_13768__$1 = state_13768;
var statearr_13774_13804 = state_13768__$1;
(statearr_13774_13804[(2)] = inst_13764);

(statearr_13774_13804[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (13))){
var inst_13757 = cljs.core.async.close_BANG_.call(null,out);
var state_13768__$1 = state_13768;
var statearr_13775_13805 = state_13768__$1;
(statearr_13775_13805[(2)] = inst_13757);

(statearr_13775_13805[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (6))){
var state_13768__$1 = state_13768;
var statearr_13776_13806 = state_13768__$1;
(statearr_13776_13806[(2)] = null);

(statearr_13776_13806[(1)] = (11));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (3))){
var inst_13766 = (state_13768[(2)]);
var state_13768__$1 = state_13768;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_13768__$1,inst_13766);
} else {
if((state_val_13769 === (12))){
var inst_13754 = (state_13768[(8)]);
var inst_13754__$1 = (state_13768[(2)]);
var inst_13755 = cljs.core.some.call(null,cljs.core.nil_QMARK_,inst_13754__$1);
var state_13768__$1 = (function (){var statearr_13777 = state_13768;
(statearr_13777[(8)] = inst_13754__$1);

return statearr_13777;
})();
if(cljs.core.truth_(inst_13755)){
var statearr_13778_13807 = state_13768__$1;
(statearr_13778_13807[(1)] = (13));

} else {
var statearr_13779_13808 = state_13768__$1;
(statearr_13779_13808[(1)] = (14));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (2))){
var inst_13731 = cljs.core.reset_BANG_.call(null,dctr,cnt);
var inst_13732 = (0);
var state_13768__$1 = (function (){var statearr_13780 = state_13768;
(statearr_13780[(9)] = inst_13731);

(statearr_13780[(7)] = inst_13732);

return statearr_13780;
})();
var statearr_13781_13809 = state_13768__$1;
(statearr_13781_13809[(2)] = null);

(statearr_13781_13809[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (11))){
var inst_13732 = (state_13768[(7)]);
var _ = cljs.core.async.impl.ioc_helpers.add_exception_frame.call(null,state_13768,(10),Object,null,(9));
var inst_13741 = chs__$1.call(null,inst_13732);
var inst_13742 = done.call(null,inst_13732);
var inst_13743 = cljs.core.async.take_BANG_.call(null,inst_13741,inst_13742);
var state_13768__$1 = state_13768;
var statearr_13782_13810 = state_13768__$1;
(statearr_13782_13810[(2)] = inst_13743);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13768__$1);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (9))){
var inst_13732 = (state_13768[(7)]);
var inst_13745 = (state_13768[(2)]);
var inst_13746 = (inst_13732 + (1));
var inst_13732__$1 = inst_13746;
var state_13768__$1 = (function (){var statearr_13783 = state_13768;
(statearr_13783[(10)] = inst_13745);

(statearr_13783[(7)] = inst_13732__$1);

return statearr_13783;
})();
var statearr_13784_13811 = state_13768__$1;
(statearr_13784_13811[(2)] = null);

(statearr_13784_13811[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (5))){
var inst_13752 = (state_13768[(2)]);
var state_13768__$1 = (function (){var statearr_13785 = state_13768;
(statearr_13785[(11)] = inst_13752);

return statearr_13785;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_13768__$1,(12),dchan);
} else {
if((state_val_13769 === (14))){
var inst_13754 = (state_13768[(8)]);
var inst_13759 = cljs.core.apply.call(null,f,inst_13754);
var state_13768__$1 = state_13768;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_13768__$1,(16),out,inst_13759);
} else {
if((state_val_13769 === (16))){
var inst_13761 = (state_13768[(2)]);
var state_13768__$1 = (function (){var statearr_13786 = state_13768;
(statearr_13786[(12)] = inst_13761);

return statearr_13786;
})();
var statearr_13787_13812 = state_13768__$1;
(statearr_13787_13812[(2)] = null);

(statearr_13787_13812[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (10))){
var inst_13736 = (state_13768[(2)]);
var inst_13737 = cljs.core.swap_BANG_.call(null,dctr,cljs.core.dec);
var state_13768__$1 = (function (){var statearr_13788 = state_13768;
(statearr_13788[(13)] = inst_13736);

return statearr_13788;
})();
var statearr_13789_13813 = state_13768__$1;
(statearr_13789_13813[(2)] = inst_13737);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13768__$1);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13769 === (8))){
var inst_13750 = (state_13768[(2)]);
var state_13768__$1 = state_13768;
var statearr_13790_13814 = state_13768__$1;
(statearr_13790_13814[(2)] = inst_13750);

(statearr_13790_13814[(1)] = (5));


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
}
}
}
}
}
}
}
}
});})(c__6803__auto___13799,chs__$1,out,cnt,rets,dchan,dctr,done))
;
return ((function (switch__6741__auto__,c__6803__auto___13799,chs__$1,out,cnt,rets,dchan,dctr,done){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_13794 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_13794[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_13794[(1)] = (1));

return statearr_13794;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_13768){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_13768);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e13795){if((e13795 instanceof Object)){
var ex__6745__auto__ = e13795;
var statearr_13796_13815 = state_13768;
(statearr_13796_13815[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13768);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e13795;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__13816 = state_13768;
state_13768 = G__13816;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_13768){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_13768);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___13799,chs__$1,out,cnt,rets,dchan,dctr,done))
})();
var state__6805__auto__ = (function (){var statearr_13797 = f__6804__auto__.call(null);
(statearr_13797[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___13799);

return statearr_13797;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___13799,chs__$1,out,cnt,rets,dchan,dctr,done))
);


return out;
});

cljs.core.async.map.cljs$lang$maxFixedArity = 3;
/**
 * Takes a collection of source channels and returns a channel which
 * contains all values taken from them. The returned channel will be
 * unbuffered by default, or a buf-or-n can be supplied. The channel
 * will close after all the source channels have closed.
 */
cljs.core.async.merge = (function cljs$core$async$merge(){
var G__13819 = arguments.length;
switch (G__13819) {
case 1:
return cljs.core.async.merge.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.merge.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.merge.cljs$core$IFn$_invoke$arity$1 = (function (chs){
return cljs.core.async.merge.call(null,chs,null);
});

cljs.core.async.merge.cljs$core$IFn$_invoke$arity$2 = (function (chs,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__6803__auto___13874 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___13874,out){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___13874,out){
return (function (state_13849){
var state_val_13850 = (state_13849[(1)]);
if((state_val_13850 === (7))){
var inst_13828 = (state_13849[(7)]);
var inst_13829 = (state_13849[(8)]);
var inst_13828__$1 = (state_13849[(2)]);
var inst_13829__$1 = cljs.core.nth.call(null,inst_13828__$1,(0),null);
var inst_13830 = cljs.core.nth.call(null,inst_13828__$1,(1),null);
var inst_13831 = (inst_13829__$1 == null);
var state_13849__$1 = (function (){var statearr_13851 = state_13849;
(statearr_13851[(9)] = inst_13830);

(statearr_13851[(7)] = inst_13828__$1);

(statearr_13851[(8)] = inst_13829__$1);

return statearr_13851;
})();
if(cljs.core.truth_(inst_13831)){
var statearr_13852_13875 = state_13849__$1;
(statearr_13852_13875[(1)] = (8));

} else {
var statearr_13853_13876 = state_13849__$1;
(statearr_13853_13876[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13850 === (1))){
var inst_13820 = cljs.core.vec.call(null,chs);
var inst_13821 = inst_13820;
var state_13849__$1 = (function (){var statearr_13854 = state_13849;
(statearr_13854[(10)] = inst_13821);

return statearr_13854;
})();
var statearr_13855_13877 = state_13849__$1;
(statearr_13855_13877[(2)] = null);

(statearr_13855_13877[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13850 === (4))){
var inst_13821 = (state_13849[(10)]);
var state_13849__$1 = state_13849;
return cljs.core.async.ioc_alts_BANG_.call(null,state_13849__$1,(7),inst_13821);
} else {
if((state_val_13850 === (6))){
var inst_13845 = (state_13849[(2)]);
var state_13849__$1 = state_13849;
var statearr_13856_13878 = state_13849__$1;
(statearr_13856_13878[(2)] = inst_13845);

(statearr_13856_13878[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13850 === (3))){
var inst_13847 = (state_13849[(2)]);
var state_13849__$1 = state_13849;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_13849__$1,inst_13847);
} else {
if((state_val_13850 === (2))){
var inst_13821 = (state_13849[(10)]);
var inst_13823 = cljs.core.count.call(null,inst_13821);
var inst_13824 = (inst_13823 > (0));
var state_13849__$1 = state_13849;
if(cljs.core.truth_(inst_13824)){
var statearr_13858_13879 = state_13849__$1;
(statearr_13858_13879[(1)] = (4));

} else {
var statearr_13859_13880 = state_13849__$1;
(statearr_13859_13880[(1)] = (5));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13850 === (11))){
var inst_13821 = (state_13849[(10)]);
var inst_13838 = (state_13849[(2)]);
var tmp13857 = inst_13821;
var inst_13821__$1 = tmp13857;
var state_13849__$1 = (function (){var statearr_13860 = state_13849;
(statearr_13860[(10)] = inst_13821__$1);

(statearr_13860[(11)] = inst_13838);

return statearr_13860;
})();
var statearr_13861_13881 = state_13849__$1;
(statearr_13861_13881[(2)] = null);

(statearr_13861_13881[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13850 === (9))){
var inst_13829 = (state_13849[(8)]);
var state_13849__$1 = state_13849;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_13849__$1,(11),out,inst_13829);
} else {
if((state_val_13850 === (5))){
var inst_13843 = cljs.core.async.close_BANG_.call(null,out);
var state_13849__$1 = state_13849;
var statearr_13862_13882 = state_13849__$1;
(statearr_13862_13882[(2)] = inst_13843);

(statearr_13862_13882[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13850 === (10))){
var inst_13841 = (state_13849[(2)]);
var state_13849__$1 = state_13849;
var statearr_13863_13883 = state_13849__$1;
(statearr_13863_13883[(2)] = inst_13841);

(statearr_13863_13883[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13850 === (8))){
var inst_13821 = (state_13849[(10)]);
var inst_13830 = (state_13849[(9)]);
var inst_13828 = (state_13849[(7)]);
var inst_13829 = (state_13849[(8)]);
var inst_13833 = (function (){var cs = inst_13821;
var vec__13826 = inst_13828;
var v = inst_13829;
var c = inst_13830;
return ((function (cs,vec__13826,v,c,inst_13821,inst_13830,inst_13828,inst_13829,state_val_13850,c__6803__auto___13874,out){
return (function (p1__13817_SHARP_){
return cljs.core.not_EQ_.call(null,c,p1__13817_SHARP_);
});
;})(cs,vec__13826,v,c,inst_13821,inst_13830,inst_13828,inst_13829,state_val_13850,c__6803__auto___13874,out))
})();
var inst_13834 = cljs.core.filterv.call(null,inst_13833,inst_13821);
var inst_13821__$1 = inst_13834;
var state_13849__$1 = (function (){var statearr_13864 = state_13849;
(statearr_13864[(10)] = inst_13821__$1);

return statearr_13864;
})();
var statearr_13865_13884 = state_13849__$1;
(statearr_13865_13884[(2)] = null);

(statearr_13865_13884[(1)] = (2));


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
}
}
}
});})(c__6803__auto___13874,out))
;
return ((function (switch__6741__auto__,c__6803__auto___13874,out){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_13869 = [null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_13869[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_13869[(1)] = (1));

return statearr_13869;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_13849){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_13849);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e13870){if((e13870 instanceof Object)){
var ex__6745__auto__ = e13870;
var statearr_13871_13885 = state_13849;
(statearr_13871_13885[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13849);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e13870;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__13886 = state_13849;
state_13849 = G__13886;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_13849){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_13849);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___13874,out))
})();
var state__6805__auto__ = (function (){var statearr_13872 = f__6804__auto__.call(null);
(statearr_13872[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___13874);

return statearr_13872;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___13874,out))
);


return out;
});

cljs.core.async.merge.cljs$lang$maxFixedArity = 2;
/**
 * Returns a channel containing the single (collection) result of the
 * items taken from the channel conjoined to the supplied
 * collection. ch must close before into produces a result.
 */
cljs.core.async.into = (function cljs$core$async$into(coll,ch){
return cljs.core.async.reduce.call(null,cljs.core.conj,coll,ch);
});
/**
 * Returns a channel that will return, at most, n items from ch. After n items
 * have been returned, or ch has been closed, the return chanel will close.
 * 
 * The output channel is unbuffered by default, unless buf-or-n is given.
 */
cljs.core.async.take = (function cljs$core$async$take(){
var G__13888 = arguments.length;
switch (G__13888) {
case 2:
return cljs.core.async.take.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.take.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.take.cljs$core$IFn$_invoke$arity$2 = (function (n,ch){
return cljs.core.async.take.call(null,n,ch,null);
});

cljs.core.async.take.cljs$core$IFn$_invoke$arity$3 = (function (n,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__6803__auto___13936 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___13936,out){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___13936,out){
return (function (state_13912){
var state_val_13913 = (state_13912[(1)]);
if((state_val_13913 === (7))){
var inst_13894 = (state_13912[(7)]);
var inst_13894__$1 = (state_13912[(2)]);
var inst_13895 = (inst_13894__$1 == null);
var inst_13896 = cljs.core.not.call(null,inst_13895);
var state_13912__$1 = (function (){var statearr_13914 = state_13912;
(statearr_13914[(7)] = inst_13894__$1);

return statearr_13914;
})();
if(inst_13896){
var statearr_13915_13937 = state_13912__$1;
(statearr_13915_13937[(1)] = (8));

} else {
var statearr_13916_13938 = state_13912__$1;
(statearr_13916_13938[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (1))){
var inst_13889 = (0);
var state_13912__$1 = (function (){var statearr_13917 = state_13912;
(statearr_13917[(8)] = inst_13889);

return statearr_13917;
})();
var statearr_13918_13939 = state_13912__$1;
(statearr_13918_13939[(2)] = null);

(statearr_13918_13939[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (4))){
var state_13912__$1 = state_13912;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_13912__$1,(7),ch);
} else {
if((state_val_13913 === (6))){
var inst_13907 = (state_13912[(2)]);
var state_13912__$1 = state_13912;
var statearr_13919_13940 = state_13912__$1;
(statearr_13919_13940[(2)] = inst_13907);

(statearr_13919_13940[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (3))){
var inst_13909 = (state_13912[(2)]);
var inst_13910 = cljs.core.async.close_BANG_.call(null,out);
var state_13912__$1 = (function (){var statearr_13920 = state_13912;
(statearr_13920[(9)] = inst_13909);

return statearr_13920;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_13912__$1,inst_13910);
} else {
if((state_val_13913 === (2))){
var inst_13889 = (state_13912[(8)]);
var inst_13891 = (inst_13889 < n);
var state_13912__$1 = state_13912;
if(cljs.core.truth_(inst_13891)){
var statearr_13921_13941 = state_13912__$1;
(statearr_13921_13941[(1)] = (4));

} else {
var statearr_13922_13942 = state_13912__$1;
(statearr_13922_13942[(1)] = (5));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (11))){
var inst_13889 = (state_13912[(8)]);
var inst_13899 = (state_13912[(2)]);
var inst_13900 = (inst_13889 + (1));
var inst_13889__$1 = inst_13900;
var state_13912__$1 = (function (){var statearr_13923 = state_13912;
(statearr_13923[(10)] = inst_13899);

(statearr_13923[(8)] = inst_13889__$1);

return statearr_13923;
})();
var statearr_13924_13943 = state_13912__$1;
(statearr_13924_13943[(2)] = null);

(statearr_13924_13943[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (9))){
var state_13912__$1 = state_13912;
var statearr_13925_13944 = state_13912__$1;
(statearr_13925_13944[(2)] = null);

(statearr_13925_13944[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (5))){
var state_13912__$1 = state_13912;
var statearr_13926_13945 = state_13912__$1;
(statearr_13926_13945[(2)] = null);

(statearr_13926_13945[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (10))){
var inst_13904 = (state_13912[(2)]);
var state_13912__$1 = state_13912;
var statearr_13927_13946 = state_13912__$1;
(statearr_13927_13946[(2)] = inst_13904);

(statearr_13927_13946[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13913 === (8))){
var inst_13894 = (state_13912[(7)]);
var state_13912__$1 = state_13912;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_13912__$1,(11),out,inst_13894);
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
}
}
}
});})(c__6803__auto___13936,out))
;
return ((function (switch__6741__auto__,c__6803__auto___13936,out){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_13931 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_13931[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_13931[(1)] = (1));

return statearr_13931;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_13912){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_13912);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e13932){if((e13932 instanceof Object)){
var ex__6745__auto__ = e13932;
var statearr_13933_13947 = state_13912;
(statearr_13933_13947[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13912);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e13932;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__13948 = state_13912;
state_13912 = G__13948;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_13912){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_13912);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___13936,out))
})();
var state__6805__auto__ = (function (){var statearr_13934 = f__6804__auto__.call(null);
(statearr_13934[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___13936);

return statearr_13934;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___13936,out))
);


return out;
});

cljs.core.async.take.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.map_LT_ = (function cljs$core$async$map_LT_(f,ch){
if(typeof cljs.core.async.t13956 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t13956 = (function (map_LT_,f,ch,meta13957){
this.map_LT_ = map_LT_;
this.f = f;
this.ch = ch;
this.meta13957 = meta13957;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t13956.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_13958,meta13957__$1){
var self__ = this;
var _13958__$1 = this;
return (new cljs.core.async.t13956(self__.map_LT_,self__.f,self__.ch,meta13957__$1));
});

cljs.core.async.t13956.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_13958){
var self__ = this;
var _13958__$1 = this;
return self__.meta13957;
});

cljs.core.async.t13956.prototype.cljs$core$async$impl$protocols$Channel$ = true;

cljs.core.async.t13956.prototype.cljs$core$async$impl$protocols$Channel$close_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.close_BANG_.call(null,self__.ch);
});

cljs.core.async.t13956.prototype.cljs$core$async$impl$protocols$Channel$closed_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.closed_QMARK_.call(null,self__.ch);
});

cljs.core.async.t13956.prototype.cljs$core$async$impl$protocols$ReadPort$ = true;

cljs.core.async.t13956.prototype.cljs$core$async$impl$protocols$ReadPort$take_BANG_$arity$2 = (function (_,fn1){
var self__ = this;
var ___$1 = this;
var ret = cljs.core.async.impl.protocols.take_BANG_.call(null,self__.ch,(function (){
if(typeof cljs.core.async.t13959 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t13959 = (function (map_LT_,f,ch,meta13957,_,fn1,meta13960){
this.map_LT_ = map_LT_;
this.f = f;
this.ch = ch;
this.meta13957 = meta13957;
this._ = _;
this.fn1 = fn1;
this.meta13960 = meta13960;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t13959.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (___$1){
return (function (_13961,meta13960__$1){
var self__ = this;
var _13961__$1 = this;
return (new cljs.core.async.t13959(self__.map_LT_,self__.f,self__.ch,self__.meta13957,self__._,self__.fn1,meta13960__$1));
});})(___$1))
;

cljs.core.async.t13959.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (___$1){
return (function (_13961){
var self__ = this;
var _13961__$1 = this;
return self__.meta13960;
});})(___$1))
;

cljs.core.async.t13959.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t13959.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = ((function (___$1){
return (function (___$1){
var self__ = this;
var ___$2 = this;
return cljs.core.async.impl.protocols.active_QMARK_.call(null,self__.fn1);
});})(___$1))
;

cljs.core.async.t13959.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = ((function (___$1){
return (function (___$1){
var self__ = this;
var ___$2 = this;
var f1 = cljs.core.async.impl.protocols.commit.call(null,self__.fn1);
return ((function (f1,___$2,___$1){
return (function (p1__13949_SHARP_){
return f1.call(null,(((p1__13949_SHARP_ == null))?null:self__.f.call(null,p1__13949_SHARP_)));
});
;})(f1,___$2,___$1))
});})(___$1))
;

cljs.core.async.t13959.getBasis = ((function (___$1){
return (function (){
return new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"map<","map<",-1235808357,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta13957","meta13957",2050616089,null),new cljs.core.Symbol(null,"_","_",-1201019570,null),new cljs.core.Symbol(null,"fn1","fn1",895834444,null),new cljs.core.Symbol(null,"meta13960","meta13960",-2034685618,null)], null);
});})(___$1))
;

cljs.core.async.t13959.cljs$lang$type = true;

cljs.core.async.t13959.cljs$lang$ctorStr = "cljs.core.async/t13959";

cljs.core.async.t13959.cljs$lang$ctorPrWriter = ((function (___$1){
return (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t13959");
});})(___$1))
;

cljs.core.async.__GT_t13959 = ((function (___$1){
return (function cljs$core$async$map_LT__$___GT_t13959(map_LT___$1,f__$1,ch__$1,meta13957__$1,___$2,fn1__$1,meta13960){
return (new cljs.core.async.t13959(map_LT___$1,f__$1,ch__$1,meta13957__$1,___$2,fn1__$1,meta13960));
});})(___$1))
;

}

return (new cljs.core.async.t13959(self__.map_LT_,self__.f,self__.ch,self__.meta13957,___$1,fn1,cljs.core.PersistentArrayMap.EMPTY));
})()
);
if(cljs.core.truth_((function (){var and__4309__auto__ = ret;
if(cljs.core.truth_(and__4309__auto__)){
return !((cljs.core.deref.call(null,ret) == null));
} else {
return and__4309__auto__;
}
})())){
return cljs.core.async.impl.channels.box.call(null,self__.f.call(null,cljs.core.deref.call(null,ret)));
} else {
return ret;
}
});

cljs.core.async.t13956.prototype.cljs$core$async$impl$protocols$WritePort$ = true;

cljs.core.async.t13956.prototype.cljs$core$async$impl$protocols$WritePort$put_BANG_$arity$3 = (function (_,val,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.put_BANG_.call(null,self__.ch,val,fn1);
});

cljs.core.async.t13956.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"map<","map<",-1235808357,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta13957","meta13957",2050616089,null)], null);
});

cljs.core.async.t13956.cljs$lang$type = true;

cljs.core.async.t13956.cljs$lang$ctorStr = "cljs.core.async/t13956";

cljs.core.async.t13956.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t13956");
});

cljs.core.async.__GT_t13956 = (function cljs$core$async$map_LT__$___GT_t13956(map_LT___$1,f__$1,ch__$1,meta13957){
return (new cljs.core.async.t13956(map_LT___$1,f__$1,ch__$1,meta13957));
});

}

return (new cljs.core.async.t13956(cljs$core$async$map_LT_,f,ch,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.map_GT_ = (function cljs$core$async$map_GT_(f,ch){
if(typeof cljs.core.async.t13965 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t13965 = (function (map_GT_,f,ch,meta13966){
this.map_GT_ = map_GT_;
this.f = f;
this.ch = ch;
this.meta13966 = meta13966;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t13965.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_13967,meta13966__$1){
var self__ = this;
var _13967__$1 = this;
return (new cljs.core.async.t13965(self__.map_GT_,self__.f,self__.ch,meta13966__$1));
});

cljs.core.async.t13965.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_13967){
var self__ = this;
var _13967__$1 = this;
return self__.meta13966;
});

cljs.core.async.t13965.prototype.cljs$core$async$impl$protocols$Channel$ = true;

cljs.core.async.t13965.prototype.cljs$core$async$impl$protocols$Channel$close_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.close_BANG_.call(null,self__.ch);
});

cljs.core.async.t13965.prototype.cljs$core$async$impl$protocols$ReadPort$ = true;

cljs.core.async.t13965.prototype.cljs$core$async$impl$protocols$ReadPort$take_BANG_$arity$2 = (function (_,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.take_BANG_.call(null,self__.ch,fn1);
});

cljs.core.async.t13965.prototype.cljs$core$async$impl$protocols$WritePort$ = true;

cljs.core.async.t13965.prototype.cljs$core$async$impl$protocols$WritePort$put_BANG_$arity$3 = (function (_,val,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.put_BANG_.call(null,self__.ch,self__.f.call(null,val),fn1);
});

cljs.core.async.t13965.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"map>","map>",1676369295,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta13966","meta13966",1102914402,null)], null);
});

cljs.core.async.t13965.cljs$lang$type = true;

cljs.core.async.t13965.cljs$lang$ctorStr = "cljs.core.async/t13965";

cljs.core.async.t13965.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t13965");
});

cljs.core.async.__GT_t13965 = (function cljs$core$async$map_GT__$___GT_t13965(map_GT___$1,f__$1,ch__$1,meta13966){
return (new cljs.core.async.t13965(map_GT___$1,f__$1,ch__$1,meta13966));
});

}

return (new cljs.core.async.t13965(cljs$core$async$map_GT_,f,ch,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.filter_GT_ = (function cljs$core$async$filter_GT_(p,ch){
if(typeof cljs.core.async.t13971 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t13971 = (function (filter_GT_,p,ch,meta13972){
this.filter_GT_ = filter_GT_;
this.p = p;
this.ch = ch;
this.meta13972 = meta13972;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t13971.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_13973,meta13972__$1){
var self__ = this;
var _13973__$1 = this;
return (new cljs.core.async.t13971(self__.filter_GT_,self__.p,self__.ch,meta13972__$1));
});

cljs.core.async.t13971.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_13973){
var self__ = this;
var _13973__$1 = this;
return self__.meta13972;
});

cljs.core.async.t13971.prototype.cljs$core$async$impl$protocols$Channel$ = true;

cljs.core.async.t13971.prototype.cljs$core$async$impl$protocols$Channel$close_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.close_BANG_.call(null,self__.ch);
});

cljs.core.async.t13971.prototype.cljs$core$async$impl$protocols$Channel$closed_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.closed_QMARK_.call(null,self__.ch);
});

cljs.core.async.t13971.prototype.cljs$core$async$impl$protocols$ReadPort$ = true;

cljs.core.async.t13971.prototype.cljs$core$async$impl$protocols$ReadPort$take_BANG_$arity$2 = (function (_,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.take_BANG_.call(null,self__.ch,fn1);
});

cljs.core.async.t13971.prototype.cljs$core$async$impl$protocols$WritePort$ = true;

cljs.core.async.t13971.prototype.cljs$core$async$impl$protocols$WritePort$put_BANG_$arity$3 = (function (_,val,fn1){
var self__ = this;
var ___$1 = this;
if(cljs.core.truth_(self__.p.call(null,val))){
return cljs.core.async.impl.protocols.put_BANG_.call(null,self__.ch,val,fn1);
} else {
return cljs.core.async.impl.channels.box.call(null,cljs.core.not.call(null,cljs.core.async.impl.protocols.closed_QMARK_.call(null,self__.ch)));
}
});

cljs.core.async.t13971.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"filter>","filter>",-37644455,null),new cljs.core.Symbol(null,"p","p",1791580836,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta13972","meta13972",-868694359,null)], null);
});

cljs.core.async.t13971.cljs$lang$type = true;

cljs.core.async.t13971.cljs$lang$ctorStr = "cljs.core.async/t13971";

cljs.core.async.t13971.cljs$lang$ctorPrWriter = (function (this__4900__auto__,writer__4901__auto__,opt__4902__auto__){
return cljs.core._write.call(null,writer__4901__auto__,"cljs.core.async/t13971");
});

cljs.core.async.__GT_t13971 = (function cljs$core$async$filter_GT__$___GT_t13971(filter_GT___$1,p__$1,ch__$1,meta13972){
return (new cljs.core.async.t13971(filter_GT___$1,p__$1,ch__$1,meta13972));
});

}

return (new cljs.core.async.t13971(cljs$core$async$filter_GT_,p,ch,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.remove_GT_ = (function cljs$core$async$remove_GT_(p,ch){
return cljs.core.async.filter_GT_.call(null,cljs.core.complement.call(null,p),ch);
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.filter_LT_ = (function cljs$core$async$filter_LT_(){
var G__13975 = arguments.length;
switch (G__13975) {
case 2:
return cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$2 = (function (p,ch){
return cljs.core.async.filter_LT_.call(null,p,ch,null);
});

cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$3 = (function (p,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__6803__auto___14018 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___14018,out){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___14018,out){
return (function (state_13996){
var state_val_13997 = (state_13996[(1)]);
if((state_val_13997 === (7))){
var inst_13992 = (state_13996[(2)]);
var state_13996__$1 = state_13996;
var statearr_13998_14019 = state_13996__$1;
(statearr_13998_14019[(2)] = inst_13992);

(statearr_13998_14019[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (1))){
var state_13996__$1 = state_13996;
var statearr_13999_14020 = state_13996__$1;
(statearr_13999_14020[(2)] = null);

(statearr_13999_14020[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (4))){
var inst_13978 = (state_13996[(7)]);
var inst_13978__$1 = (state_13996[(2)]);
var inst_13979 = (inst_13978__$1 == null);
var state_13996__$1 = (function (){var statearr_14000 = state_13996;
(statearr_14000[(7)] = inst_13978__$1);

return statearr_14000;
})();
if(cljs.core.truth_(inst_13979)){
var statearr_14001_14021 = state_13996__$1;
(statearr_14001_14021[(1)] = (5));

} else {
var statearr_14002_14022 = state_13996__$1;
(statearr_14002_14022[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (6))){
var inst_13978 = (state_13996[(7)]);
var inst_13983 = p.call(null,inst_13978);
var state_13996__$1 = state_13996;
if(cljs.core.truth_(inst_13983)){
var statearr_14003_14023 = state_13996__$1;
(statearr_14003_14023[(1)] = (8));

} else {
var statearr_14004_14024 = state_13996__$1;
(statearr_14004_14024[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (3))){
var inst_13994 = (state_13996[(2)]);
var state_13996__$1 = state_13996;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_13996__$1,inst_13994);
} else {
if((state_val_13997 === (2))){
var state_13996__$1 = state_13996;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_13996__$1,(4),ch);
} else {
if((state_val_13997 === (11))){
var inst_13986 = (state_13996[(2)]);
var state_13996__$1 = state_13996;
var statearr_14005_14025 = state_13996__$1;
(statearr_14005_14025[(2)] = inst_13986);

(statearr_14005_14025[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (9))){
var state_13996__$1 = state_13996;
var statearr_14006_14026 = state_13996__$1;
(statearr_14006_14026[(2)] = null);

(statearr_14006_14026[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (5))){
var inst_13981 = cljs.core.async.close_BANG_.call(null,out);
var state_13996__$1 = state_13996;
var statearr_14007_14027 = state_13996__$1;
(statearr_14007_14027[(2)] = inst_13981);

(statearr_14007_14027[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (10))){
var inst_13989 = (state_13996[(2)]);
var state_13996__$1 = (function (){var statearr_14008 = state_13996;
(statearr_14008[(8)] = inst_13989);

return statearr_14008;
})();
var statearr_14009_14028 = state_13996__$1;
(statearr_14009_14028[(2)] = null);

(statearr_14009_14028[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_13997 === (8))){
var inst_13978 = (state_13996[(7)]);
var state_13996__$1 = state_13996;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_13996__$1,(11),out,inst_13978);
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
}
}
}
});})(c__6803__auto___14018,out))
;
return ((function (switch__6741__auto__,c__6803__auto___14018,out){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_14013 = [null,null,null,null,null,null,null,null,null];
(statearr_14013[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_14013[(1)] = (1));

return statearr_14013;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_13996){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_13996);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e14014){if((e14014 instanceof Object)){
var ex__6745__auto__ = e14014;
var statearr_14015_14029 = state_13996;
(statearr_14015_14029[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_13996);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e14014;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__14030 = state_13996;
state_13996 = G__14030;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_13996){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_13996);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___14018,out))
})();
var state__6805__auto__ = (function (){var statearr_14016 = f__6804__auto__.call(null);
(statearr_14016[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___14018);

return statearr_14016;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___14018,out))
);


return out;
});

cljs.core.async.filter_LT_.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.remove_LT_ = (function cljs$core$async$remove_LT_(){
var G__14032 = arguments.length;
switch (G__14032) {
case 2:
return cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$2 = (function (p,ch){
return cljs.core.async.remove_LT_.call(null,p,ch,null);
});

cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$3 = (function (p,ch,buf_or_n){
return cljs.core.async.filter_LT_.call(null,cljs.core.complement.call(null,p),ch,buf_or_n);
});

cljs.core.async.remove_LT_.cljs$lang$maxFixedArity = 3;
cljs.core.async.mapcat_STAR_ = (function cljs$core$async$mapcat_STAR_(f,in$,out){
var c__6803__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto__){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto__){
return (function (state_14199){
var state_val_14200 = (state_14199[(1)]);
if((state_val_14200 === (7))){
var inst_14195 = (state_14199[(2)]);
var state_14199__$1 = state_14199;
var statearr_14201_14242 = state_14199__$1;
(statearr_14201_14242[(2)] = inst_14195);

(statearr_14201_14242[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (20))){
var inst_14165 = (state_14199[(7)]);
var inst_14176 = (state_14199[(2)]);
var inst_14177 = cljs.core.next.call(null,inst_14165);
var inst_14151 = inst_14177;
var inst_14152 = null;
var inst_14153 = (0);
var inst_14154 = (0);
var state_14199__$1 = (function (){var statearr_14202 = state_14199;
(statearr_14202[(8)] = inst_14176);

(statearr_14202[(9)] = inst_14154);

(statearr_14202[(10)] = inst_14151);

(statearr_14202[(11)] = inst_14153);

(statearr_14202[(12)] = inst_14152);

return statearr_14202;
})();
var statearr_14203_14243 = state_14199__$1;
(statearr_14203_14243[(2)] = null);

(statearr_14203_14243[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (1))){
var state_14199__$1 = state_14199;
var statearr_14204_14244 = state_14199__$1;
(statearr_14204_14244[(2)] = null);

(statearr_14204_14244[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (4))){
var inst_14140 = (state_14199[(13)]);
var inst_14140__$1 = (state_14199[(2)]);
var inst_14141 = (inst_14140__$1 == null);
var state_14199__$1 = (function (){var statearr_14205 = state_14199;
(statearr_14205[(13)] = inst_14140__$1);

return statearr_14205;
})();
if(cljs.core.truth_(inst_14141)){
var statearr_14206_14245 = state_14199__$1;
(statearr_14206_14245[(1)] = (5));

} else {
var statearr_14207_14246 = state_14199__$1;
(statearr_14207_14246[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (15))){
var state_14199__$1 = state_14199;
var statearr_14211_14247 = state_14199__$1;
(statearr_14211_14247[(2)] = null);

(statearr_14211_14247[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (21))){
var state_14199__$1 = state_14199;
var statearr_14212_14248 = state_14199__$1;
(statearr_14212_14248[(2)] = null);

(statearr_14212_14248[(1)] = (23));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (13))){
var inst_14154 = (state_14199[(9)]);
var inst_14151 = (state_14199[(10)]);
var inst_14153 = (state_14199[(11)]);
var inst_14152 = (state_14199[(12)]);
var inst_14161 = (state_14199[(2)]);
var inst_14162 = (inst_14154 + (1));
var tmp14208 = inst_14151;
var tmp14209 = inst_14153;
var tmp14210 = inst_14152;
var inst_14151__$1 = tmp14208;
var inst_14152__$1 = tmp14210;
var inst_14153__$1 = tmp14209;
var inst_14154__$1 = inst_14162;
var state_14199__$1 = (function (){var statearr_14213 = state_14199;
(statearr_14213[(14)] = inst_14161);

(statearr_14213[(9)] = inst_14154__$1);

(statearr_14213[(10)] = inst_14151__$1);

(statearr_14213[(11)] = inst_14153__$1);

(statearr_14213[(12)] = inst_14152__$1);

return statearr_14213;
})();
var statearr_14214_14249 = state_14199__$1;
(statearr_14214_14249[(2)] = null);

(statearr_14214_14249[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (22))){
var state_14199__$1 = state_14199;
var statearr_14215_14250 = state_14199__$1;
(statearr_14215_14250[(2)] = null);

(statearr_14215_14250[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (6))){
var inst_14140 = (state_14199[(13)]);
var inst_14149 = f.call(null,inst_14140);
var inst_14150 = cljs.core.seq.call(null,inst_14149);
var inst_14151 = inst_14150;
var inst_14152 = null;
var inst_14153 = (0);
var inst_14154 = (0);
var state_14199__$1 = (function (){var statearr_14216 = state_14199;
(statearr_14216[(9)] = inst_14154);

(statearr_14216[(10)] = inst_14151);

(statearr_14216[(11)] = inst_14153);

(statearr_14216[(12)] = inst_14152);

return statearr_14216;
})();
var statearr_14217_14251 = state_14199__$1;
(statearr_14217_14251[(2)] = null);

(statearr_14217_14251[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (17))){
var inst_14165 = (state_14199[(7)]);
var inst_14169 = cljs.core.chunk_first.call(null,inst_14165);
var inst_14170 = cljs.core.chunk_rest.call(null,inst_14165);
var inst_14171 = cljs.core.count.call(null,inst_14169);
var inst_14151 = inst_14170;
var inst_14152 = inst_14169;
var inst_14153 = inst_14171;
var inst_14154 = (0);
var state_14199__$1 = (function (){var statearr_14218 = state_14199;
(statearr_14218[(9)] = inst_14154);

(statearr_14218[(10)] = inst_14151);

(statearr_14218[(11)] = inst_14153);

(statearr_14218[(12)] = inst_14152);

return statearr_14218;
})();
var statearr_14219_14252 = state_14199__$1;
(statearr_14219_14252[(2)] = null);

(statearr_14219_14252[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (3))){
var inst_14197 = (state_14199[(2)]);
var state_14199__$1 = state_14199;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_14199__$1,inst_14197);
} else {
if((state_val_14200 === (12))){
var inst_14185 = (state_14199[(2)]);
var state_14199__$1 = state_14199;
var statearr_14220_14253 = state_14199__$1;
(statearr_14220_14253[(2)] = inst_14185);

(statearr_14220_14253[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (2))){
var state_14199__$1 = state_14199;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_14199__$1,(4),in$);
} else {
if((state_val_14200 === (23))){
var inst_14193 = (state_14199[(2)]);
var state_14199__$1 = state_14199;
var statearr_14221_14254 = state_14199__$1;
(statearr_14221_14254[(2)] = inst_14193);

(statearr_14221_14254[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (19))){
var inst_14180 = (state_14199[(2)]);
var state_14199__$1 = state_14199;
var statearr_14222_14255 = state_14199__$1;
(statearr_14222_14255[(2)] = inst_14180);

(statearr_14222_14255[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (11))){
var inst_14151 = (state_14199[(10)]);
var inst_14165 = (state_14199[(7)]);
var inst_14165__$1 = cljs.core.seq.call(null,inst_14151);
var state_14199__$1 = (function (){var statearr_14223 = state_14199;
(statearr_14223[(7)] = inst_14165__$1);

return statearr_14223;
})();
if(inst_14165__$1){
var statearr_14224_14256 = state_14199__$1;
(statearr_14224_14256[(1)] = (14));

} else {
var statearr_14225_14257 = state_14199__$1;
(statearr_14225_14257[(1)] = (15));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (9))){
var inst_14187 = (state_14199[(2)]);
var inst_14188 = cljs.core.async.impl.protocols.closed_QMARK_.call(null,out);
var state_14199__$1 = (function (){var statearr_14226 = state_14199;
(statearr_14226[(15)] = inst_14187);

return statearr_14226;
})();
if(cljs.core.truth_(inst_14188)){
var statearr_14227_14258 = state_14199__$1;
(statearr_14227_14258[(1)] = (21));

} else {
var statearr_14228_14259 = state_14199__$1;
(statearr_14228_14259[(1)] = (22));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (5))){
var inst_14143 = cljs.core.async.close_BANG_.call(null,out);
var state_14199__$1 = state_14199;
var statearr_14229_14260 = state_14199__$1;
(statearr_14229_14260[(2)] = inst_14143);

(statearr_14229_14260[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (14))){
var inst_14165 = (state_14199[(7)]);
var inst_14167 = cljs.core.chunked_seq_QMARK_.call(null,inst_14165);
var state_14199__$1 = state_14199;
if(inst_14167){
var statearr_14230_14261 = state_14199__$1;
(statearr_14230_14261[(1)] = (17));

} else {
var statearr_14231_14262 = state_14199__$1;
(statearr_14231_14262[(1)] = (18));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (16))){
var inst_14183 = (state_14199[(2)]);
var state_14199__$1 = state_14199;
var statearr_14232_14263 = state_14199__$1;
(statearr_14232_14263[(2)] = inst_14183);

(statearr_14232_14263[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14200 === (10))){
var inst_14154 = (state_14199[(9)]);
var inst_14152 = (state_14199[(12)]);
var inst_14159 = cljs.core._nth.call(null,inst_14152,inst_14154);
var state_14199__$1 = state_14199;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_14199__$1,(13),out,inst_14159);
} else {
if((state_val_14200 === (18))){
var inst_14165 = (state_14199[(7)]);
var inst_14174 = cljs.core.first.call(null,inst_14165);
var state_14199__$1 = state_14199;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_14199__$1,(20),out,inst_14174);
} else {
if((state_val_14200 === (8))){
var inst_14154 = (state_14199[(9)]);
var inst_14153 = (state_14199[(11)]);
var inst_14156 = (inst_14154 < inst_14153);
var inst_14157 = inst_14156;
var state_14199__$1 = state_14199;
if(cljs.core.truth_(inst_14157)){
var statearr_14233_14264 = state_14199__$1;
(statearr_14233_14264[(1)] = (10));

} else {
var statearr_14234_14265 = state_14199__$1;
(statearr_14234_14265[(1)] = (11));

}

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
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__6803__auto__))
;
return ((function (switch__6741__auto__,c__6803__auto__){
return (function() {
var cljs$core$async$mapcat_STAR__$_state_machine__6742__auto__ = null;
var cljs$core$async$mapcat_STAR__$_state_machine__6742__auto____0 = (function (){
var statearr_14238 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_14238[(0)] = cljs$core$async$mapcat_STAR__$_state_machine__6742__auto__);

(statearr_14238[(1)] = (1));

return statearr_14238;
});
var cljs$core$async$mapcat_STAR__$_state_machine__6742__auto____1 = (function (state_14199){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_14199);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e14239){if((e14239 instanceof Object)){
var ex__6745__auto__ = e14239;
var statearr_14240_14266 = state_14199;
(statearr_14240_14266[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_14199);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e14239;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__14267 = state_14199;
state_14199 = G__14267;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$mapcat_STAR__$_state_machine__6742__auto__ = function(state_14199){
switch(arguments.length){
case 0:
return cljs$core$async$mapcat_STAR__$_state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$mapcat_STAR__$_state_machine__6742__auto____1.call(this,state_14199);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$mapcat_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$mapcat_STAR__$_state_machine__6742__auto____0;
cljs$core$async$mapcat_STAR__$_state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$mapcat_STAR__$_state_machine__6742__auto____1;
return cljs$core$async$mapcat_STAR__$_state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto__))
})();
var state__6805__auto__ = (function (){var statearr_14241 = f__6804__auto__.call(null);
(statearr_14241[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto__);

return statearr_14241;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto__))
);

return c__6803__auto__;
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.mapcat_LT_ = (function cljs$core$async$mapcat_LT_(){
var G__14269 = arguments.length;
switch (G__14269) {
case 2:
return cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$2 = (function (f,in$){
return cljs.core.async.mapcat_LT_.call(null,f,in$,null);
});

cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$3 = (function (f,in$,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
cljs.core.async.mapcat_STAR_.call(null,f,in$,out);

return out;
});

cljs.core.async.mapcat_LT_.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.mapcat_GT_ = (function cljs$core$async$mapcat_GT_(){
var G__14272 = arguments.length;
switch (G__14272) {
case 2:
return cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$2 = (function (f,out){
return cljs.core.async.mapcat_GT_.call(null,f,out,null);
});

cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$3 = (function (f,out,buf_or_n){
var in$ = cljs.core.async.chan.call(null,buf_or_n);
cljs.core.async.mapcat_STAR_.call(null,f,in$,out);

return in$;
});

cljs.core.async.mapcat_GT_.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.unique = (function cljs$core$async$unique(){
var G__14275 = arguments.length;
switch (G__14275) {
case 1:
return cljs.core.async.unique.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.unique.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.unique.cljs$core$IFn$_invoke$arity$1 = (function (ch){
return cljs.core.async.unique.call(null,ch,null);
});

cljs.core.async.unique.cljs$core$IFn$_invoke$arity$2 = (function (ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__6803__auto___14325 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___14325,out){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___14325,out){
return (function (state_14299){
var state_val_14300 = (state_14299[(1)]);
if((state_val_14300 === (7))){
var inst_14294 = (state_14299[(2)]);
var state_14299__$1 = state_14299;
var statearr_14301_14326 = state_14299__$1;
(statearr_14301_14326[(2)] = inst_14294);

(statearr_14301_14326[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14300 === (1))){
var inst_14276 = null;
var state_14299__$1 = (function (){var statearr_14302 = state_14299;
(statearr_14302[(7)] = inst_14276);

return statearr_14302;
})();
var statearr_14303_14327 = state_14299__$1;
(statearr_14303_14327[(2)] = null);

(statearr_14303_14327[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14300 === (4))){
var inst_14279 = (state_14299[(8)]);
var inst_14279__$1 = (state_14299[(2)]);
var inst_14280 = (inst_14279__$1 == null);
var inst_14281 = cljs.core.not.call(null,inst_14280);
var state_14299__$1 = (function (){var statearr_14304 = state_14299;
(statearr_14304[(8)] = inst_14279__$1);

return statearr_14304;
})();
if(inst_14281){
var statearr_14305_14328 = state_14299__$1;
(statearr_14305_14328[(1)] = (5));

} else {
var statearr_14306_14329 = state_14299__$1;
(statearr_14306_14329[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14300 === (6))){
var state_14299__$1 = state_14299;
var statearr_14307_14330 = state_14299__$1;
(statearr_14307_14330[(2)] = null);

(statearr_14307_14330[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14300 === (3))){
var inst_14296 = (state_14299[(2)]);
var inst_14297 = cljs.core.async.close_BANG_.call(null,out);
var state_14299__$1 = (function (){var statearr_14308 = state_14299;
(statearr_14308[(9)] = inst_14296);

return statearr_14308;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_14299__$1,inst_14297);
} else {
if((state_val_14300 === (2))){
var state_14299__$1 = state_14299;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_14299__$1,(4),ch);
} else {
if((state_val_14300 === (11))){
var inst_14279 = (state_14299[(8)]);
var inst_14288 = (state_14299[(2)]);
var inst_14276 = inst_14279;
var state_14299__$1 = (function (){var statearr_14309 = state_14299;
(statearr_14309[(7)] = inst_14276);

(statearr_14309[(10)] = inst_14288);

return statearr_14309;
})();
var statearr_14310_14331 = state_14299__$1;
(statearr_14310_14331[(2)] = null);

(statearr_14310_14331[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14300 === (9))){
var inst_14279 = (state_14299[(8)]);
var state_14299__$1 = state_14299;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_14299__$1,(11),out,inst_14279);
} else {
if((state_val_14300 === (5))){
var inst_14276 = (state_14299[(7)]);
var inst_14279 = (state_14299[(8)]);
var inst_14283 = cljs.core._EQ_.call(null,inst_14279,inst_14276);
var state_14299__$1 = state_14299;
if(inst_14283){
var statearr_14312_14332 = state_14299__$1;
(statearr_14312_14332[(1)] = (8));

} else {
var statearr_14313_14333 = state_14299__$1;
(statearr_14313_14333[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14300 === (10))){
var inst_14291 = (state_14299[(2)]);
var state_14299__$1 = state_14299;
var statearr_14314_14334 = state_14299__$1;
(statearr_14314_14334[(2)] = inst_14291);

(statearr_14314_14334[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14300 === (8))){
var inst_14276 = (state_14299[(7)]);
var tmp14311 = inst_14276;
var inst_14276__$1 = tmp14311;
var state_14299__$1 = (function (){var statearr_14315 = state_14299;
(statearr_14315[(7)] = inst_14276__$1);

return statearr_14315;
})();
var statearr_14316_14335 = state_14299__$1;
(statearr_14316_14335[(2)] = null);

(statearr_14316_14335[(1)] = (2));


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
}
}
}
});})(c__6803__auto___14325,out))
;
return ((function (switch__6741__auto__,c__6803__auto___14325,out){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_14320 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_14320[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_14320[(1)] = (1));

return statearr_14320;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_14299){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_14299);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e14321){if((e14321 instanceof Object)){
var ex__6745__auto__ = e14321;
var statearr_14322_14336 = state_14299;
(statearr_14322_14336[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_14299);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e14321;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__14337 = state_14299;
state_14299 = G__14337;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_14299){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_14299);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___14325,out))
})();
var state__6805__auto__ = (function (){var statearr_14323 = f__6804__auto__.call(null);
(statearr_14323[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___14325);

return statearr_14323;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___14325,out))
);


return out;
});

cljs.core.async.unique.cljs$lang$maxFixedArity = 2;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.partition = (function cljs$core$async$partition(){
var G__14339 = arguments.length;
switch (G__14339) {
case 2:
return cljs.core.async.partition.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.partition.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.partition.cljs$core$IFn$_invoke$arity$2 = (function (n,ch){
return cljs.core.async.partition.call(null,n,ch,null);
});

cljs.core.async.partition.cljs$core$IFn$_invoke$arity$3 = (function (n,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__6803__auto___14408 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___14408,out){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___14408,out){
return (function (state_14377){
var state_val_14378 = (state_14377[(1)]);
if((state_val_14378 === (7))){
var inst_14373 = (state_14377[(2)]);
var state_14377__$1 = state_14377;
var statearr_14379_14409 = state_14377__$1;
(statearr_14379_14409[(2)] = inst_14373);

(statearr_14379_14409[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (1))){
var inst_14340 = (new Array(n));
var inst_14341 = inst_14340;
var inst_14342 = (0);
var state_14377__$1 = (function (){var statearr_14380 = state_14377;
(statearr_14380[(7)] = inst_14342);

(statearr_14380[(8)] = inst_14341);

return statearr_14380;
})();
var statearr_14381_14410 = state_14377__$1;
(statearr_14381_14410[(2)] = null);

(statearr_14381_14410[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (4))){
var inst_14345 = (state_14377[(9)]);
var inst_14345__$1 = (state_14377[(2)]);
var inst_14346 = (inst_14345__$1 == null);
var inst_14347 = cljs.core.not.call(null,inst_14346);
var state_14377__$1 = (function (){var statearr_14382 = state_14377;
(statearr_14382[(9)] = inst_14345__$1);

return statearr_14382;
})();
if(inst_14347){
var statearr_14383_14411 = state_14377__$1;
(statearr_14383_14411[(1)] = (5));

} else {
var statearr_14384_14412 = state_14377__$1;
(statearr_14384_14412[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (15))){
var inst_14367 = (state_14377[(2)]);
var state_14377__$1 = state_14377;
var statearr_14385_14413 = state_14377__$1;
(statearr_14385_14413[(2)] = inst_14367);

(statearr_14385_14413[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (13))){
var state_14377__$1 = state_14377;
var statearr_14386_14414 = state_14377__$1;
(statearr_14386_14414[(2)] = null);

(statearr_14386_14414[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (6))){
var inst_14342 = (state_14377[(7)]);
var inst_14363 = (inst_14342 > (0));
var state_14377__$1 = state_14377;
if(cljs.core.truth_(inst_14363)){
var statearr_14387_14415 = state_14377__$1;
(statearr_14387_14415[(1)] = (12));

} else {
var statearr_14388_14416 = state_14377__$1;
(statearr_14388_14416[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (3))){
var inst_14375 = (state_14377[(2)]);
var state_14377__$1 = state_14377;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_14377__$1,inst_14375);
} else {
if((state_val_14378 === (12))){
var inst_14341 = (state_14377[(8)]);
var inst_14365 = cljs.core.vec.call(null,inst_14341);
var state_14377__$1 = state_14377;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_14377__$1,(15),out,inst_14365);
} else {
if((state_val_14378 === (2))){
var state_14377__$1 = state_14377;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_14377__$1,(4),ch);
} else {
if((state_val_14378 === (11))){
var inst_14357 = (state_14377[(2)]);
var inst_14358 = (new Array(n));
var inst_14341 = inst_14358;
var inst_14342 = (0);
var state_14377__$1 = (function (){var statearr_14389 = state_14377;
(statearr_14389[(7)] = inst_14342);

(statearr_14389[(10)] = inst_14357);

(statearr_14389[(8)] = inst_14341);

return statearr_14389;
})();
var statearr_14390_14417 = state_14377__$1;
(statearr_14390_14417[(2)] = null);

(statearr_14390_14417[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (9))){
var inst_14341 = (state_14377[(8)]);
var inst_14355 = cljs.core.vec.call(null,inst_14341);
var state_14377__$1 = state_14377;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_14377__$1,(11),out,inst_14355);
} else {
if((state_val_14378 === (5))){
var inst_14350 = (state_14377[(11)]);
var inst_14345 = (state_14377[(9)]);
var inst_14342 = (state_14377[(7)]);
var inst_14341 = (state_14377[(8)]);
var inst_14349 = (inst_14341[inst_14342] = inst_14345);
var inst_14350__$1 = (inst_14342 + (1));
var inst_14351 = (inst_14350__$1 < n);
var state_14377__$1 = (function (){var statearr_14391 = state_14377;
(statearr_14391[(11)] = inst_14350__$1);

(statearr_14391[(12)] = inst_14349);

return statearr_14391;
})();
if(cljs.core.truth_(inst_14351)){
var statearr_14392_14418 = state_14377__$1;
(statearr_14392_14418[(1)] = (8));

} else {
var statearr_14393_14419 = state_14377__$1;
(statearr_14393_14419[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (14))){
var inst_14370 = (state_14377[(2)]);
var inst_14371 = cljs.core.async.close_BANG_.call(null,out);
var state_14377__$1 = (function (){var statearr_14395 = state_14377;
(statearr_14395[(13)] = inst_14370);

return statearr_14395;
})();
var statearr_14396_14420 = state_14377__$1;
(statearr_14396_14420[(2)] = inst_14371);

(statearr_14396_14420[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (10))){
var inst_14361 = (state_14377[(2)]);
var state_14377__$1 = state_14377;
var statearr_14397_14421 = state_14377__$1;
(statearr_14397_14421[(2)] = inst_14361);

(statearr_14397_14421[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14378 === (8))){
var inst_14350 = (state_14377[(11)]);
var inst_14341 = (state_14377[(8)]);
var tmp14394 = inst_14341;
var inst_14341__$1 = tmp14394;
var inst_14342 = inst_14350;
var state_14377__$1 = (function (){var statearr_14398 = state_14377;
(statearr_14398[(7)] = inst_14342);

(statearr_14398[(8)] = inst_14341__$1);

return statearr_14398;
})();
var statearr_14399_14422 = state_14377__$1;
(statearr_14399_14422[(2)] = null);

(statearr_14399_14422[(1)] = (2));


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
}
}
}
}
}
}
}
});})(c__6803__auto___14408,out))
;
return ((function (switch__6741__auto__,c__6803__auto___14408,out){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_14403 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_14403[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_14403[(1)] = (1));

return statearr_14403;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_14377){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_14377);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e14404){if((e14404 instanceof Object)){
var ex__6745__auto__ = e14404;
var statearr_14405_14423 = state_14377;
(statearr_14405_14423[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_14377);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e14404;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__14424 = state_14377;
state_14377 = G__14424;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_14377){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_14377);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___14408,out))
})();
var state__6805__auto__ = (function (){var statearr_14406 = f__6804__auto__.call(null);
(statearr_14406[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___14408);

return statearr_14406;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___14408,out))
);


return out;
});

cljs.core.async.partition.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.partition_by = (function cljs$core$async$partition_by(){
var G__14426 = arguments.length;
switch (G__14426) {
case 2:
return cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$2 = (function (f,ch){
return cljs.core.async.partition_by.call(null,f,ch,null);
});

cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$3 = (function (f,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__6803__auto___14499 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__6803__auto___14499,out){
return (function (){
var f__6804__auto__ = (function (){var switch__6741__auto__ = ((function (c__6803__auto___14499,out){
return (function (state_14468){
var state_val_14469 = (state_14468[(1)]);
if((state_val_14469 === (7))){
var inst_14464 = (state_14468[(2)]);
var state_14468__$1 = state_14468;
var statearr_14470_14500 = state_14468__$1;
(statearr_14470_14500[(2)] = inst_14464);

(statearr_14470_14500[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (1))){
var inst_14427 = [];
var inst_14428 = inst_14427;
var inst_14429 = new cljs.core.Keyword("cljs.core.async","nothing","cljs.core.async/nothing",-69252123);
var state_14468__$1 = (function (){var statearr_14471 = state_14468;
(statearr_14471[(7)] = inst_14429);

(statearr_14471[(8)] = inst_14428);

return statearr_14471;
})();
var statearr_14472_14501 = state_14468__$1;
(statearr_14472_14501[(2)] = null);

(statearr_14472_14501[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (4))){
var inst_14432 = (state_14468[(9)]);
var inst_14432__$1 = (state_14468[(2)]);
var inst_14433 = (inst_14432__$1 == null);
var inst_14434 = cljs.core.not.call(null,inst_14433);
var state_14468__$1 = (function (){var statearr_14473 = state_14468;
(statearr_14473[(9)] = inst_14432__$1);

return statearr_14473;
})();
if(inst_14434){
var statearr_14474_14502 = state_14468__$1;
(statearr_14474_14502[(1)] = (5));

} else {
var statearr_14475_14503 = state_14468__$1;
(statearr_14475_14503[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (15))){
var inst_14458 = (state_14468[(2)]);
var state_14468__$1 = state_14468;
var statearr_14476_14504 = state_14468__$1;
(statearr_14476_14504[(2)] = inst_14458);

(statearr_14476_14504[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (13))){
var state_14468__$1 = state_14468;
var statearr_14477_14505 = state_14468__$1;
(statearr_14477_14505[(2)] = null);

(statearr_14477_14505[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (6))){
var inst_14428 = (state_14468[(8)]);
var inst_14453 = inst_14428.length;
var inst_14454 = (inst_14453 > (0));
var state_14468__$1 = state_14468;
if(cljs.core.truth_(inst_14454)){
var statearr_14478_14506 = state_14468__$1;
(statearr_14478_14506[(1)] = (12));

} else {
var statearr_14479_14507 = state_14468__$1;
(statearr_14479_14507[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (3))){
var inst_14466 = (state_14468[(2)]);
var state_14468__$1 = state_14468;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_14468__$1,inst_14466);
} else {
if((state_val_14469 === (12))){
var inst_14428 = (state_14468[(8)]);
var inst_14456 = cljs.core.vec.call(null,inst_14428);
var state_14468__$1 = state_14468;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_14468__$1,(15),out,inst_14456);
} else {
if((state_val_14469 === (2))){
var state_14468__$1 = state_14468;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_14468__$1,(4),ch);
} else {
if((state_val_14469 === (11))){
var inst_14436 = (state_14468[(10)]);
var inst_14432 = (state_14468[(9)]);
var inst_14446 = (state_14468[(2)]);
var inst_14447 = [];
var inst_14448 = inst_14447.push(inst_14432);
var inst_14428 = inst_14447;
var inst_14429 = inst_14436;
var state_14468__$1 = (function (){var statearr_14480 = state_14468;
(statearr_14480[(7)] = inst_14429);

(statearr_14480[(11)] = inst_14446);

(statearr_14480[(12)] = inst_14448);

(statearr_14480[(8)] = inst_14428);

return statearr_14480;
})();
var statearr_14481_14508 = state_14468__$1;
(statearr_14481_14508[(2)] = null);

(statearr_14481_14508[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (9))){
var inst_14428 = (state_14468[(8)]);
var inst_14444 = cljs.core.vec.call(null,inst_14428);
var state_14468__$1 = state_14468;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_14468__$1,(11),out,inst_14444);
} else {
if((state_val_14469 === (5))){
var inst_14436 = (state_14468[(10)]);
var inst_14429 = (state_14468[(7)]);
var inst_14432 = (state_14468[(9)]);
var inst_14436__$1 = f.call(null,inst_14432);
var inst_14437 = cljs.core._EQ_.call(null,inst_14436__$1,inst_14429);
var inst_14438 = cljs.core.keyword_identical_QMARK_.call(null,inst_14429,new cljs.core.Keyword("cljs.core.async","nothing","cljs.core.async/nothing",-69252123));
var inst_14439 = (inst_14437) || (inst_14438);
var state_14468__$1 = (function (){var statearr_14482 = state_14468;
(statearr_14482[(10)] = inst_14436__$1);

return statearr_14482;
})();
if(cljs.core.truth_(inst_14439)){
var statearr_14483_14509 = state_14468__$1;
(statearr_14483_14509[(1)] = (8));

} else {
var statearr_14484_14510 = state_14468__$1;
(statearr_14484_14510[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (14))){
var inst_14461 = (state_14468[(2)]);
var inst_14462 = cljs.core.async.close_BANG_.call(null,out);
var state_14468__$1 = (function (){var statearr_14486 = state_14468;
(statearr_14486[(13)] = inst_14461);

return statearr_14486;
})();
var statearr_14487_14511 = state_14468__$1;
(statearr_14487_14511[(2)] = inst_14462);

(statearr_14487_14511[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (10))){
var inst_14451 = (state_14468[(2)]);
var state_14468__$1 = state_14468;
var statearr_14488_14512 = state_14468__$1;
(statearr_14488_14512[(2)] = inst_14451);

(statearr_14488_14512[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_14469 === (8))){
var inst_14436 = (state_14468[(10)]);
var inst_14432 = (state_14468[(9)]);
var inst_14428 = (state_14468[(8)]);
var inst_14441 = inst_14428.push(inst_14432);
var tmp14485 = inst_14428;
var inst_14428__$1 = tmp14485;
var inst_14429 = inst_14436;
var state_14468__$1 = (function (){var statearr_14489 = state_14468;
(statearr_14489[(7)] = inst_14429);

(statearr_14489[(14)] = inst_14441);

(statearr_14489[(8)] = inst_14428__$1);

return statearr_14489;
})();
var statearr_14490_14513 = state_14468__$1;
(statearr_14490_14513[(2)] = null);

(statearr_14490_14513[(1)] = (2));


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
}
}
}
}
}
}
}
});})(c__6803__auto___14499,out))
;
return ((function (switch__6741__auto__,c__6803__auto___14499,out){
return (function() {
var cljs$core$async$state_machine__6742__auto__ = null;
var cljs$core$async$state_machine__6742__auto____0 = (function (){
var statearr_14494 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_14494[(0)] = cljs$core$async$state_machine__6742__auto__);

(statearr_14494[(1)] = (1));

return statearr_14494;
});
var cljs$core$async$state_machine__6742__auto____1 = (function (state_14468){
while(true){
var ret_value__6743__auto__ = (function (){try{while(true){
var result__6744__auto__ = switch__6741__auto__.call(null,state_14468);
if(cljs.core.keyword_identical_QMARK_.call(null,result__6744__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__6744__auto__;
}
break;
}
}catch (e14495){if((e14495 instanceof Object)){
var ex__6745__auto__ = e14495;
var statearr_14496_14514 = state_14468;
(statearr_14496_14514[(5)] = ex__6745__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_14468);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e14495;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__6743__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__14515 = state_14468;
state_14468 = G__14515;
continue;
} else {
return ret_value__6743__auto__;
}
break;
}
});
cljs$core$async$state_machine__6742__auto__ = function(state_14468){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__6742__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__6742__auto____1.call(this,state_14468);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__6742__auto____0;
cljs$core$async$state_machine__6742__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__6742__auto____1;
return cljs$core$async$state_machine__6742__auto__;
})()
;})(switch__6741__auto__,c__6803__auto___14499,out))
})();
var state__6805__auto__ = (function (){var statearr_14497 = f__6804__auto__.call(null);
(statearr_14497[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__6803__auto___14499);

return statearr_14497;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__6805__auto__);
});})(c__6803__auto___14499,out))
);


return out;
});

cljs.core.async.partition_by.cljs$lang$maxFixedArity = 3;

//# sourceMappingURL=async.js.map