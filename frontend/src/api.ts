export type StoredAuth = {
	username: string;
	accessToken: string;
	refreshToken: string;
};

export type Role = "COMMON" | "MODERATOR" | "ADMIN";

export type PostPermissionType = "MODIFY" | "DELETE" | "DELETE_COMMENTS";

export type UserResponse = {
	id: string;
	username: string;
	email: string;
	role: Role;
};

export type PostPermissionResponse = {
	id: string;
	granteeId: string;
	granteeUsername: string;
	permissionType: PostPermissionType;
};

export type PostResponse = {
	id: string;
	title: string;
	content: string;
	userId: string;
	authorUsername?: string;
	createdAt?: string;
	updatedAt?: string;
};

export type CommentResponse = {
	id: string;
	content: string;
	postId: string;
	authorId: string;
	authorUsername: string;
	createdAt?: string;
	updatedAt?: string;
};

export type LoginRequest = {
	username: string;
	password: string;
};

export type RegisterRequest = {
	username: string;
	email: string;
	password: string;
};

export type PostRequest = {
	title: string;
	content: string;
};

export type CommentRequest = {
	content: string;
};

export type GrantAccessRequest = {
	granteeUserId: string;
	permissions: PostPermissionType[];
};

export type AuthResponse = {
	accessToken: string;
	refreshToken: string;
};

export type RegisterResponse = {
	user: UserResponse;
	accessToken: string;
	refreshToken: string;
};

export type RefreshResponse = {
	accessToken: string;
};

type ApiEnvelope<T> = {
	data: T;
	error?: string | null;
	timestamp?: string;
};

export class ApiError extends Error {
	status: number;
	payload: unknown;

	constructor(message: string, status: number, payload?: unknown) {
		super(message);
		this.name = "ApiError";
		this.status = status;
		this.payload = payload;
	}
}

export type BlogApi = {
	login: (request: LoginRequest) => Promise<AuthResponse>;
	register: (request: RegisterRequest) => Promise<RegisterResponse>;
	logout: (username: string) => Promise<void>;
	refresh: () => Promise<RefreshResponse>;
	getCurrentUser: () => Promise<UserResponse>;
	getUserByUsername: (username: string) => Promise<UserResponse>;
	getPosts: () => Promise<PostResponse[]>;
	getPost: (id: string) => Promise<PostResponse>;
	createPost: (request: PostRequest) => Promise<PostResponse>;
	updatePost: (id: string, request: PostRequest) => Promise<PostResponse>;
	deletePost: (id: string) => Promise<PostResponse>;
	getComments: (postId: string) => Promise<CommentResponse[]>;
	getComment: (postId: string, commentId: string) => Promise<CommentResponse>;
	createComment: (
		postId: string,
		request: CommentRequest,
	) => Promise<CommentResponse>;
	updateComment: (
		postId: string,
		commentId: string,
		request: CommentRequest,
	) => Promise<CommentResponse>;
	deleteComment: (
		postId: string,
		commentId: string,
	) => Promise<CommentResponse>;
	getPostAccess: (postId: string) => Promise<PostPermissionResponse[]>;
	getMyPostAccess: (postId: string) => Promise<PostPermissionType[]>;
	grantAccess: (postId: string, request: GrantAccessRequest) => Promise<void>;
	revokeAccess: (postId: string, request: GrantAccessRequest) => Promise<void>;
};

export function createBlogApi(
	baseUrl: string,
	auth: StoredAuth | null,
	setAuth: (auth: StoredAuth | null) => void,
): BlogApi {
	const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");

	const buildUrl = (path: string) => `${normalizedBaseUrl}${path}`;

	const readBody = async <T>(response: Response): Promise<T> => {
		const text = await response.text();

		if (!text) {
			return undefined as T;
		}

		return JSON.parse(text) as T;
	};

	const extractErrorMessage = (
		payload: unknown,
		statusText: string,
		fallback: string,
	) => {
		if (payload && typeof payload === "object" && "error" in payload) {
			const envelope = payload as { error?: unknown };
			if (typeof envelope.error === "string" && envelope.error.trim()) {
				return envelope.error;
			}
		}

		if (typeof payload === "string" && payload.trim()) {
			return payload;
		}

		return statusText || fallback;
	};

	const request = async <T>(
		path: string,
		init: RequestInit = {},
		options: { authRequired?: boolean; retryCount?: number } = {},
	): Promise<T> => {
		const authRequired = options.authRequired !== false;
		const retryCount = options.retryCount ?? 0;
		const headers = new Headers(init.headers);

		if (
			init.body &&
			!(init.body instanceof FormData) &&
			!headers.has("Content-Type")
		) {
			headers.set("Content-Type", "application/json");
		}

		if (authRequired && auth?.accessToken) {
			headers.set("Authorization", `Bearer ${auth.accessToken}`);
		}

		const response = await fetch(buildUrl(path), {
			...init,
			headers,
		});

		if (
			response.status === 401 &&
			authRequired &&
			auth?.refreshToken &&
			retryCount === 0
		) {
			try {
				const refreshed = await request<RefreshResponse>(
					"/api/auth/refresh",
					{
						method: "POST",
						headers: {
							"Content-Type": "application/json",
						},
						body: JSON.stringify({ refreshToken: auth.refreshToken }),
					},
					{ authRequired: false },
				);

				if (!refreshed.accessToken) {
					throw new ApiError("Session expired. Please sign in again.", 401);
				}

				setAuth({
					...auth,
					accessToken: refreshed.accessToken,
				});
				return request<T>(path, init, { authRequired, retryCount: 1 });
			} catch (error) {
				setAuth(null);
				throw error instanceof ApiError
					? error
					: new ApiError("Session expired. Please sign in again.", 401);
			}
		}

		const payload = (await readBody<unknown>(response)) as
			| ApiEnvelope<T>
			| T
			| null;

		if (!response.ok) {
			throw new ApiError(
				extractErrorMessage(payload, response.statusText, "Request failed"),
				response.status,
				payload,
			);
		}

		if (payload && typeof payload === "object" && "data" in payload) {
			return (payload as ApiEnvelope<T>).data;
		}

		return payload as T;
	};

	return {
		login: (requestBody) =>
			request<AuthResponse>(
				"/api/auth/login",
				{
					method: "POST",
					body: JSON.stringify(requestBody),
				},
				{ authRequired: false },
			),
		register: (requestBody) =>
			request<RegisterResponse>(
				"/api/auth/register",
				{
					method: "POST",
					body: JSON.stringify(requestBody),
				},
				{ authRequired: false },
			),
		logout: (username) =>
			request<void>(
				"/api/auth/logout",
				{
					method: "POST",
					body: JSON.stringify({ username }),
				},
				{ authRequired: false },
			),
		refresh: () => {
			if (!auth?.refreshToken) {
				return Promise.reject(new ApiError("No refresh token available", 401));
			}

			return request<RefreshResponse>(
				"/api/auth/refresh",
				{
					method: "POST",
					body: JSON.stringify({ refreshToken: auth.refreshToken }),
				},
				{ authRequired: false },
			);
		},
		getCurrentUser: () => request<UserResponse>("/api/users/me"),
		getUserByUsername: (username) =>
			request<UserResponse>(
				`/api/users/by-username/${encodeURIComponent(username)}`,
			),
		getPosts: () => request<PostResponse[]>("/api/posts"),
		getPost: (id) => request<PostResponse>(`/api/posts/${id}`),
		createPost: (requestBody) =>
			request<PostResponse>("/api/posts", {
				method: "POST",
				body: JSON.stringify(requestBody),
			}),
		updatePost: (id, requestBody) =>
			request<PostResponse>(`/api/posts/${id}`, {
				method: "PUT",
				body: JSON.stringify(requestBody),
			}),
		deletePost: (id) =>
			request<PostResponse>(`/api/posts/${id}`, {
				method: "DELETE",
			}),
		getComments: (postId) =>
			request<CommentResponse[]>(`/api/posts/${postId}/comments`),
		getComment: (postId, commentId) =>
			request<CommentResponse>(`/api/posts/${postId}/comments/${commentId}`),
		createComment: (postId, requestBody) =>
			request<CommentResponse>(`/api/posts/${postId}/comments`, {
				method: "POST",
				body: JSON.stringify(requestBody),
			}),
		updateComment: (postId, commentId, requestBody) =>
			request<CommentResponse>(`/api/posts/${postId}/comments/${commentId}`, {
				method: "PUT",
				body: JSON.stringify(requestBody),
			}),
		deleteComment: (postId, commentId) =>
			request<CommentResponse>(`/api/posts/${postId}/comments/${commentId}`, {
				method: "DELETE",
			}),
		getPostAccess: (postId) =>
			request<PostPermissionResponse[]>(`/api/posts/${postId}/access`),
		getMyPostAccess: (postId) =>
			request<PostPermissionType[]>(`/api/posts/${postId}/access/mine`),
		grantAccess: (postId, requestBody) =>
			request<void>(`/api/posts/${postId}/access`, {
				method: "POST",
				body: JSON.stringify(requestBody),
			}),
		revokeAccess: (postId, requestBody) =>
			request<void>(`/api/posts/${postId}/access`, {
				method: "DELETE",
				body: JSON.stringify(requestBody),
			}),
	};
}