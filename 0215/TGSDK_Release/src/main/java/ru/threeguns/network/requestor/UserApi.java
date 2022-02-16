package ru.threeguns.network.requestor;

import kh.hyper.network.annotations.Address;
import kh.hyper.network.annotations.P;
import ru.threeguns.entity.User;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.annotations.Progress;
import ru.threeguns.network.annotations.WithToken;

public interface UserApi extends TGApi {
	//登录
	@Progress(Progress.LOGIN)
	@Address("/api/login")
	void login(//
               @P("username") String username, //
               @P("password") String password, //
               TGResultHandler handler);

	//用户注册
	@Progress(Progress.REGISTER)
	@Address("/api/register")
	@P({ "type", User.USERTYPE_NORMAL })
	void register(//
                  @P("username") String username, //
                  @P("password") String password, //
                  TGResultHandler handler);

	//游客注册
	@Progress(Progress.LOGIN)
	@Address("/api/register")
	@P({ "type", User.USERTYPE_GUEST })
	void fastRegister(//
                      @P("username") String username, //
                      @P("password") String password, //
                      TGResultHandler handler);

	//修改密码
	@Progress(Progress.LOADING)
	@Address("/api/editPwd")
	void changePassword(//
                        @P("username") String username, //
                        @P("password") String password, //
                        @P("newpassword") String newpassword, //
                        TGResultHandler handler);

	//升级账户
	@Progress(Progress.LOADING)
	@Address("/api/upgrade")
	@WithToken
	void upgrade(//
                 @P("username") String username, //
                 @P("password") String password, //
                 TGResultHandler handler);

	//获取密码
	@Progress(Progress.LOADING)
	@Address("/api/getPwd")
	void restorePassword(//
                         @P("username") String username, //
                         TGResultHandler handler);

	//设置昵称
	@Progress(Progress.LOADING)
	@Address("/api/setNickname")
	@WithToken
	void setNickname(//
                     @P("nickname") String nickname, //
                     TGResultHandler handler);

	//token登录
	@Progress(Progress.LOGIN)
	@Address("/api/tokenLogin")
	void tokenLogin(//
                    @P("user_id") String userId, //
                    @P("token") String token, //
                    TGResultHandler handler);

	//第三方登录
	@Progress(Progress.LOGIN)
	@Address("/api/logintp")
	void tpLogin(//
                 @P("tp_name") String tpName, //
                 @P("access_token") String token, //
                 @P("extra_data") String extraData, //
                 TGResultHandler handler);


	//邮箱(手机)发送验证码
	@Progress(Progress.SEND)
	@Address("/api/getCode")
	void sendTgEmail(//
                     @P("username") String username,//
                     TGResultHandler handler);

	//邮箱验证注册
	@Progress(Progress.REGISTER)
	@Address("/api/register")
	void apiregisteremail(//
                          @P("username") String username,//
                          @P("password") String password,//
                          @P("validn") String validn,//
                          TGResultHandler handler);

	//手机号验证注册
	@Progress(Progress.REGISTER)
	@Address("/api/register")
	void apiregisterphone(//
                          @P("username") String username,//
                          @P("password") String password,//
                          @P("validn") String validn,//
                          @P("type") String type,//
                          TGResultHandler handler);


	//邮箱/手机号验证修改密码
	@Progress(Progress.RESTORE)
	@Address("/api/modifyPwd")
	void apirestorepwd(//
                       @P("username") String username,//
                       @P("password") String password,//
                       @P("validn") String validn,//
                       TGResultHandler handler);


	//邮箱/手机号验证修改密码
	@Progress(Progress.RESTORE)
	@Address("/api/upgrade")
	void apibingaccount(//
                        @P("username") String username,//
                        @P("password") String password,//
                        @P("validn") String validn,//
                        @P("token") String token,//
                        @P("user_id") String userid,//
                        TGResultHandler handler);

	//google登陆
	@Address("/api/bind_gcm_token")
	void bindToken(//
                   @P("gcm_token") String token, //
                   @P("user_id") String userId, //
                   TGResultHandler handler);

	//上报角色
	@Progress(Progress.LOADING)
	@Address("/log/action")
	void Action(//
                   @P("data") String data, //
                   TGResultHandler handler);


}
