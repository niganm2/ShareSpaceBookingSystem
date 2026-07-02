import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    token: localStorage.getItem("token") || "",
    user: JSON.parse(localStorage.getItem("user") || "null"),
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
      localStorage.setItem("token", token);
    },
    SET_USER(state, user) {
      state.user = user;
      localStorage.setItem("user", JSON.stringify(user));
    },
    LOGOUT(state) {
      state.token = "";
      state.user = null;
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    },
  },
  actions: {
    login({ commit }, { token, user }) {
      commit("SET_TOKEN", token);
      commit("SET_USER", user);
    },
    logout({ commit }) {
      commit("LOGOUT");
    },
  },
  getters: {
    isLoggedIn: (state) => !!state.token,
    userRole: (state) => (state.user ? state.user.role : ""),
    userName: (state) => (state.user ? state.realName : ""),
    // 正确获取 isVerified (0=未认证,1=已认证)
    isVerified: (state) => {
      if (!state.user || state.user.isVerified == null) return 0;
      return state.user.isVerified;
    },
  },
});
