let env = import.meta.env.VITE_ENV;

let baseUrl = import.meta.env.VITE_BASE_URL;
let signKey = import.meta.env.VITE_SIGN_KEY;
let signSecret = import.meta.env.VITE_SIGN_SECRET;
let tokenHeaderName = "x-token";

export default {
	baseUrl,
	signKey,
	signSecret,
	tokenHeaderName
}

