import { useEffect, useMemo, useState, type SubmitEventHandler } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createBlogApi, type StoredAuth } from "./api";

const AUTH_STORAGE_KEY = "blog-manager-auth";
const API_BASE_URL_STORAGE_KEY = "blog-manager-api-base-url";
const DEFAULT_API_BASE_URL =
	typeof window !== "undefined" && window.location.hostname === "localhost"
		? "http://localhost:8080"
		: window.location.origin;

const panelClass =
	"rounded-[2rem] border border-white/10 bg-white/5 p-5 shadow-2xl shadow-slate-950/40 backdrop-blur-xl";
const insetPanelClass =
	"rounded-[1.5rem] border border-white/10 bg-slate-950/40 p-4";
const inputClass =
	"mt-2 w-full rounded-2xl border border-white/10 bg-slate-950/60 px-4 py-3 text-sm text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-400/60 focus:ring-2 focus:ring-cyan-400/20";
const buttonBaseClass =
	"inline-flex items-center justify-center gap-2 rounded-2xl px-4 py-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50";
const primaryButtonClass = `${buttonBaseClass} bg-cyan-300 text-slate-950 hover:bg-cyan-200`;
const secondaryButtonClass = `${buttonBaseClass} border border-white/10 bg-white/5 text-slate-100 hover:bg-white/10`;
const dangerButtonClass = `${buttonBaseClass} border border-rose-400/30 bg-rose-500/10 text-rose-100 hover:bg-rose-500/20`;

function readStoredAuth(): StoredAuth | null {
	if (typeof window === "undefined") {
		return null;
	}

	const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
	if (!raw) {
		return null;
	}

	try {
		const parsed = JSON.parse(raw) as StoredAuth;
		if (parsed.accessToken && parsed.refreshToken && parsed.username) {
			return parsed;
		}
		return null;
	} catch {
		return null;
	}
}

function readStoredBaseUrl() {
	if (typeof window === "undefined") {
		return DEFAULT_API_BASE_URL;
	}

	return (
		window.localStorage.getItem(API_BASE_URL_STORAGE_KEY) ??
		DEFAULT_API_BASE_URL
	);
}

function formatDate(value?: string) {
	if (!value) {
		return "Unknown time";
	}

	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return value;
	}

	return new Intl.DateTimeFormat("en", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(date);
}

function excerpt(value: string, limit = 180) {
	if (value.length <= limit) {
		return value;
	}

	return `${value.slice(0, limit).trimEnd()}…`;
}

function App() {
	const queryClient = useQueryClient();
	const [auth, setAuth] = useState<StoredAuth | null>(readStoredAuth);
	const [apiBaseUrl, setApiBaseUrl] = useState(readStoredBaseUrl);
	const [authMode, setAuthMode] = useState<"login" | "register">("login");
	const [authForm, setAuthForm] = useState({
		username: "",
		email: "",
		password: "",
	});
	const [selectedPostId, setSelectedPostId] = useState<string | null>(null);
	const [postForm, setPostForm] = useState({
		title: "",
		content: "",
	});
	const [commentForm, setCommentForm] = useState("");
	const [editingCommentId, setEditingCommentId] = useState<string | null>(null);
	const [search, setSearch] = useState("");

	useEffect(() => {
		if (typeof window === "undefined") {
			return;
		}

		if (auth) {
			window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
		} else {
			window.localStorage.removeItem(AUTH_STORAGE_KEY);
		}
	}, [auth]);

	useEffect(() => {
		if (typeof window === "undefined") {
			return;
		}

		window.localStorage.setItem(API_BASE_URL_STORAGE_KEY, apiBaseUrl);
	}, [apiBaseUrl]);

	const api = useMemo(
		() => createBlogApi(apiBaseUrl, auth, setAuth),
		[apiBaseUrl, auth],
	);

	const postsQuery = useQuery({
		queryKey: ["posts", apiBaseUrl, auth?.accessToken ?? "guest"],
		queryFn: api.getPosts,
		enabled: Boolean(auth?.accessToken),
	});

	const selectedPostQuery = useQuery({
		queryKey: [
			"post",
			selectedPostId,
			apiBaseUrl,
			auth?.accessToken ?? "guest",
		],
		queryFn: () => api.getPost(selectedPostId ?? ""),
		enabled: Boolean(auth?.accessToken && selectedPostId),
	});

	const commentsQuery = useQuery({
		queryKey: [
			"comments",
			selectedPostId,
			apiBaseUrl,
			auth?.accessToken ?? "guest",
		],
		queryFn: () => api.getComments(selectedPostId ?? ""),
		enabled: Boolean(auth?.accessToken && selectedPostId),
	});

	const visiblePosts = (postsQuery.data ?? []).filter((post) => {
		const haystack = `${post.title} ${post.content}`.toLowerCase();
		return haystack.includes(search.toLowerCase());
	});

	const selectedPost = selectedPostQuery.data ?? null;
	const comments = commentsQuery.data ?? [];

	useEffect(() => {
		if (!selectedPost) {
			setEditingCommentId(null);
			return;
		}

		setPostForm({
			title: selectedPost.title,
			content: selectedPost.content,
		});
	}, [selectedPost]);

	useEffect(() => {
		if (!comments.length) {
			setEditingCommentId(null);
			setCommentForm("");
			return;
		}

		if (
			editingCommentId &&
			!comments.some((comment) => comment.id === editingCommentId)
		) {
			setEditingCommentId(null);
			setCommentForm("");
		}
	}, [comments, editingCommentId]);

	useEffect(() => {
		if (!auth) {
			setSelectedPostId(null);
			setPostForm({ title: "", content: "" });
			setCommentForm("");
			setEditingCommentId(null);
		}
	}, [auth]);

	const loginMutation = useMutation({
		mutationFn: () =>
			api.login({
				username: authForm.username,
				password: authForm.password,
			}),
		onSuccess: (data) => {
			setAuth({
				username: authForm.username,
				accessToken: data.accessToken,
				refreshToken: data.refreshToken,
			});
			setSelectedPostId(null);
			setEditingCommentId(null);
			setCommentForm("");
			queryClient.invalidateQueries({ queryKey: ["posts"] });
		},
	});

	const registerMutation = useMutation({
		mutationFn: () =>
			api.register({
				username: authForm.username,
				email: authForm.email,
				password: authForm.password,
			}),
		onSuccess: (data) => {
			setAuth({
				username: authForm.username,
				accessToken: data.accessToken,
				refreshToken: data.refreshToken,
			});
			setSelectedPostId(null);
			setEditingCommentId(null);
			setCommentForm("");
			queryClient.invalidateQueries({ queryKey: ["posts"] });
		},
	});

	const logoutMutation = useMutation({
		mutationFn: () => api.logout(auth?.username ?? ""),
		onSuccess: () => {
			setAuth(null);
			setSelectedPostId(null);
			setPostForm({ title: "", content: "" });
			setCommentForm("");
			setEditingCommentId(null);
			queryClient.clear();
		},
	});

	const upsertPostMutation = useMutation({
		mutationFn: (payload: {
			id?: string;
			request: { title: string; content: string };
		}) =>
			payload.id
				? api.updatePost(payload.id, payload.request)
				: api.createPost(payload.request),
		onSuccess: (post, variables) => {
			queryClient.invalidateQueries({ queryKey: ["posts"] });
			if (variables.id) {
				queryClient.invalidateQueries({ queryKey: ["post", variables.id] });
			}
			setSelectedPostId(post.id);
			setEditingCommentId(null);
			setCommentForm("");
			setPostForm({
				title: post.title,
				content: post.content,
			});
		},
	});

	const deletePostMutation = useMutation({
		mutationFn: (postId: string) => api.deletePost(postId),
		onSuccess: (_, postId) => {
			queryClient.invalidateQueries({ queryKey: ["posts"] });
			queryClient.removeQueries({ queryKey: ["post", postId] });
			queryClient.removeQueries({ queryKey: ["comments", postId] });
			if (selectedPostId === postId) {
				setSelectedPostId(null);
				setPostForm({ title: "", content: "" });
				setCommentForm("");
				setEditingCommentId(null);
			}
		},
	});

	const upsertCommentMutation = useMutation({
		mutationFn: (payload: {
			postId: string;
			commentId?: string;
			content: string;
		}) =>
			payload.commentId
				? api.updateComment(payload.postId, payload.commentId, {
						content: payload.content,
					})
				: api.createComment(payload.postId, { content: payload.content }),
		onSuccess: (_, variables) => {
			queryClient.invalidateQueries({
				queryKey: ["comments", variables.postId],
			});
			queryClient.invalidateQueries({ queryKey: ["post", variables.postId] });
			setEditingCommentId(null);
			setCommentForm("");
		},
	});

	const deleteCommentMutation = useMutation({
		mutationFn: (payload: { postId: string; commentId: string }) =>
			api.deleteComment(payload.postId, payload.commentId),
		onSuccess: (_, variables) => {
			queryClient.invalidateQueries({
				queryKey: ["comments", variables.postId],
			});
			queryClient.invalidateQueries({ queryKey: ["post", variables.postId] });
			if (editingCommentId === variables.commentId) {
				setEditingCommentId(null);
				setCommentForm("");
			}
		},
	});

	const canMutate = Boolean(auth?.accessToken);
	const activePost =
		selectedPost ??
		(selectedPostId
			? (visiblePosts.find((post) => post.id === selectedPostId) ?? null)
			: null);
	const activePostId = activePost?.id ?? null;

	const handleAuthSubmit: SubmitEventHandler<HTMLFormElement> = (event) => {
		event.preventDefault();
		if (authMode === "login") {
			loginMutation.mutate();
			return;
		}

		registerMutation.mutate();
	};

	const handleSavePost: SubmitEventHandler<HTMLFormElement> = (event) => {
		event.preventDefault();
		if (!canMutate) {
			return;
		}

		upsertPostMutation.mutate({
			id: selectedPostId ?? undefined,
			request: {
				title: postForm.title,
				content: postForm.content,
			},
		});
	};

	const handleSaveComment: SubmitEventHandler<HTMLFormElement> = (event) => {
		event.preventDefault();
		if (!canMutate || !activePostId) {
			return;
		}

		upsertCommentMutation.mutate({
			postId: activePostId,
			commentId: editingCommentId ?? undefined,
			content: commentForm,
		});
	};

	const panelError =
		(postsQuery.error instanceof Error && postsQuery.error.message) ||
		(selectedPostQuery.error instanceof Error &&
			selectedPostQuery.error.message) ||
		(commentsQuery.error instanceof Error && commentsQuery.error.message);

	const authError =
		(loginMutation.error instanceof Error && loginMutation.error.message) ||
		(registerMutation.error instanceof Error && registerMutation.error.message);

	const mutationError =
		(upsertPostMutation.error instanceof Error &&
			upsertPostMutation.error.message) ||
		(deletePostMutation.error instanceof Error &&
			deletePostMutation.error.message) ||
		(upsertCommentMutation.error instanceof Error &&
			upsertCommentMutation.error.message) ||
		(deleteCommentMutation.error instanceof Error &&
			deleteCommentMutation.error.message) ||
		(logoutMutation.error instanceof Error && logoutMutation.error.message);

	return (
		<div className="relative min-h-screen overflow-hidden bg-[#07111f] text-slate-100">
			<div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(34,211,238,0.16),transparent_36%),radial-gradient(circle_at_80%_20%,rgba(129,140,248,0.12),transparent_28%),radial-gradient(circle_at_50%_90%,rgba(244,114,182,0.1),transparent_32%)]" />
			<div className="pointer-events-none absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-size-[72px_72px] opacity-20" />

			<main className="relative mx-auto flex min-h-screen w-full max-w-[1720px] flex-col gap-6 px-4 py-4 sm:px-6 lg:px-8">
				<header className="rounded-4xl border border-white/10 bg-white/5 px-5 py-4 shadow-2xl shadow-slate-950/40 backdrop-blur-xl sm:px-6">
					<div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
						<div className="space-y-3">
							<div className="inline-flex items-center gap-2 rounded-full border border-cyan-400/25 bg-cyan-400/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.24em] text-cyan-200">
								Blog Manager Control Room
							</div>
							<div>
								<h1 className="max-w-4xl text-3xl font-semibold tracking-[-0.04em] text-white sm:text-4xl lg:text-5xl">
									Manage posts, comments, and auth in one focused workspace.
								</h1>
								<p className="mt-3 max-w-3xl text-sm leading-6 text-slate-300 sm:text-base">
									This interface is wired to the Spring Boot API with React
									Query, bearer-token refresh, and direct CRUD for posts and
									comments.
								</p>
							</div>
						</div>

						<div className="grid gap-3 sm:grid-cols-3 xl:min-w-155">
							<div className="rounded-2xl border border-white/10 bg-slate-950/50 px-4 py-3">
								<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
									API
								</div>
								<div className="mt-1 text-sm text-white">{apiBaseUrl}</div>
							</div>
							<div className="rounded-2xl border border-white/10 bg-slate-950/50 px-4 py-3">
								<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
									Session
								</div>
								<div className="mt-1 text-sm text-white">
									{auth ? `Signed in as ${auth.username}` : "Not signed in"}
								</div>
							</div>
							<div className="rounded-2xl border border-white/10 bg-slate-950/50 px-4 py-3">
								<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
									State
								</div>
								<div className="mt-1 text-sm text-white">
									{auth
										? "Live data connected"
										: "Authenticate to load the API"}
								</div>
							</div>
						</div>
					</div>
				</header>

				<div className="grid flex-1 gap-6 xl:grid-cols-[340px_minmax(0,1fr)_420px]">
					<aside className="space-y-6">
						<section className={panelClass}>
							<div className="flex items-center justify-between gap-3">
								<div>
									<p className="text-xs uppercase tracking-[0.24em] text-cyan-200/80">
										Connection
									</p>
									<h2 className="mt-2 text-xl font-semibold text-white">
										Backend endpoint
									</h2>
								</div>
								<span
									className={`rounded-full px-3 py-1 text-xs font-semibold ${auth ? "bg-emerald-400/15 text-emerald-200" : "bg-amber-400/15 text-amber-200"}`}
								>
									{auth ? "Online" : "Waiting for auth"}
								</span>
							</div>
							<label className="mt-5 block text-sm font-medium text-slate-200">
								API base URL
								<input
									className={inputClass}
									value={apiBaseUrl}
									onChange={(event) => setApiBaseUrl(event.target.value)}
									placeholder="http://localhost:8080"
								/>
							</label>
							<p className="mt-3 text-sm leading-6 text-slate-400">
								The app retries once through{" "}
								<span className="text-slate-200">/api/auth/refresh</span> when
								an access token expires.
							</p>
						</section>

						<section className={panelClass}>
							<div className="flex rounded-2xl bg-slate-950/50 p-1 text-sm">
								<button
									type="button"
									className={`flex-1 rounded-2xl px-4 py-2.5 transition ${authMode === "login" ? "bg-white text-slate-950" : "text-slate-300 hover:text-white"}`}
									onClick={() => setAuthMode("login")}
								>
									Login
								</button>
								<button
									type="button"
									className={`flex-1 rounded-2xl px-4 py-2.5 transition ${authMode === "register" ? "bg-white text-slate-950" : "text-slate-300 hover:text-white"}`}
									onClick={() => setAuthMode("register")}
								>
									Register
								</button>
							</div>

							<form className="mt-5 space-y-4" onSubmit={handleAuthSubmit}>
								<label className="block text-sm font-medium text-slate-200">
									Username
									<input
										className={inputClass}
										value={authForm.username}
										onChange={(event) =>
											setAuthForm((current) => ({
												...current,
												username: event.target.value,
											}))
										}
										placeholder="teoryman"
									/>
								</label>
								{authMode === "register" ? (
									<label className="block text-sm font-medium text-slate-200">
										Email
										<input
											className={inputClass}
											value={authForm.email}
											onChange={(event) =>
												setAuthForm((current) => ({
													...current,
													email: event.target.value,
												}))
											}
											placeholder="you@example.com"
										/>
									</label>
								) : null}
								<label className="block text-sm font-medium text-slate-200">
									Password
									<input
										type="password"
										className={inputClass}
										value={authForm.password}
										onChange={(event) =>
											setAuthForm((current) => ({
												...current,
												password: event.target.value,
											}))
										}
										placeholder="••••••••"
									/>
								</label>

								{authError ? (
									<div className="rounded-2xl border border-rose-400/25 bg-rose-500/10 px-4 py-3 text-sm text-rose-100">
										{authError}
									</div>
								) : null}

								<button
									type="submit"
									className={primaryButtonClass}
									disabled={
										loginMutation.isPending || registerMutation.isPending
									}
								>
									{authMode === "login"
										? loginMutation.isPending
											? "Signing in…"
											: "Sign in"
										: registerMutation.isPending
											? "Creating account…"
											: "Create account"}
								</button>
							</form>
						</section>

						<section className={panelClass}>
							<div className="flex items-center justify-between gap-3">
								<div>
									<p className="text-xs uppercase tracking-[0.24em] text-cyan-200/80">
										Session
									</p>
									<h2 className="mt-2 text-xl font-semibold text-white">
										Current user
									</h2>
								</div>
								<button
									type="button"
									className={secondaryButtonClass}
									disabled={!auth || logoutMutation.isPending}
									onClick={() => logoutMutation.mutate()}
								>
									{logoutMutation.isPending ? "Logging out…" : "Logout"}
								</button>
							</div>

							{auth ? (
								<div className="mt-5 space-y-3 text-sm text-slate-300">
									<div className={insetPanelClass}>
										<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
											Username
										</div>
										<div className="mt-2 text-base text-white">
											{auth.username}
										</div>
									</div>
									<div className={insetPanelClass}>
										<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
											Access token
										</div>
										<div className="mt-2 break-all text-xs leading-5 text-slate-200">
											{auth.accessToken.slice(0, 24)}…
											{auth.accessToken.slice(-12)}
										</div>
									</div>
									<div className={insetPanelClass}>
										<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
											Refresh token
										</div>
										<div className="mt-2 break-all text-xs leading-5 text-slate-200">
											{auth.refreshToken.slice(0, 24)}…
											{auth.refreshToken.slice(-12)}
										</div>
									</div>
								</div>
							) : (
								<p className="mt-5 text-sm leading-6 text-slate-400">
									Sign in or register to load posts, inspect comments, and
									exercise the full API.
								</p>
							)}
						</section>
					</aside>

					<section className="space-y-6">
						<div className="grid gap-4 md:grid-cols-4">
							<div className={panelClass}>
								<div className="text-xs uppercase tracking-[0.24em] text-slate-400">
									Posts
								</div>
								<div className="mt-3 text-3xl font-semibold text-white">
									{postsQuery.data?.length ?? 0}
								</div>
							</div>
							<div className={panelClass}>
								<div className="text-xs uppercase tracking-[0.24em] text-slate-400">
									Visible
								</div>
								<div className="mt-3 text-3xl font-semibold text-white">
									{visiblePosts.length}
								</div>
							</div>
							<div className={panelClass}>
								<div className="text-xs uppercase tracking-[0.24em] text-slate-400">
									Selected
								</div>
								<div className="mt-3 text-3xl font-semibold text-white">
									{selectedPost ? "1" : "0"}
								</div>
							</div>
							<div className={panelClass}>
								<div className="text-xs uppercase tracking-[0.24em] text-slate-400">
									Comments
								</div>
								<div className="mt-3 text-3xl font-semibold text-white">
									{comments.length}
								</div>
							</div>
						</div>

						<section className={panelClass}>
							<div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
								<div>
									<p className="text-xs uppercase tracking-[0.24em] text-cyan-200/80">
										Composer
									</p>
									<h2 className="mt-2 text-2xl font-semibold text-white">
										{selectedPost ? "Edit post" : "Create a post"}
									</h2>
									<p className="mt-2 text-sm text-slate-400">
										The same panel handles create and edit based on the selected
										post.
									</p>
								</div>
								<div className="flex flex-wrap gap-3">
									<button
										type="button"
										className={secondaryButtonClass}
										disabled={!auth}
										onClick={() => {
											setSelectedPostId(null);
											setPostForm({ title: "", content: "" });
											setCommentForm("");
											setEditingCommentId(null);
										}}
									>
										New post
									</button>
									{selectedPost ? (
										<button
											type="button"
											className={dangerButtonClass}
											disabled={!auth || deletePostMutation.isPending}
											onClick={() => deletePostMutation.mutate(selectedPost.id)}
										>
											{deletePostMutation.isPending
												? "Deleting…"
												: "Delete selected"}
										</button>
									) : null}
								</div>
							</div>

							<form className="mt-5 space-y-4" onSubmit={handleSavePost}>
								<div className="grid gap-4 lg:grid-cols-[1fr_1fr]">
									<label className="block text-sm font-medium text-slate-200">
										Title
										<input
											className={inputClass}
											value={postForm.title}
											onChange={(event) =>
												setPostForm((current) => ({
													...current,
													title: event.target.value,
												}))
											}
											placeholder="A sharper blog post title"
											disabled={!auth}
										/>
									</label>
									<div className="rounded-2xl border border-white/10 bg-slate-950/40 px-4 py-3 text-sm text-slate-400">
										{selectedPost ? (
											<>
												Editing post{" "}
												<span className="text-slate-200">
													{selectedPost.id}
												</span>
											</>
										) : (
											"Compose a new post, then select it from the list to add comments."
										)}
									</div>
								</div>
								<label className="block text-sm font-medium text-slate-200">
									Content
									<textarea
										className={`${inputClass} min-h-45 resize-y`}
										value={postForm.content}
										onChange={(event) =>
											setPostForm((current) => ({
												...current,
												content: event.target.value,
											}))
										}
										placeholder="Write a post body that is easy to scan, then update it live."
										disabled={!auth}
									/>
								</label>

								{mutationError ? (
									<div className="rounded-2xl border border-rose-400/25 bg-rose-500/10 px-4 py-3 text-sm text-rose-100">
										{mutationError}
									</div>
								) : null}

								<div className="flex flex-wrap items-center gap-3">
									<button
										type="submit"
										className={primaryButtonClass}
										disabled={!auth || upsertPostMutation.isPending}
									>
										{upsertPostMutation.isPending
											? selectedPost
												? "Saving…"
												: "Publishing…"
											: selectedPost
												? "Save changes"
												: "Publish post"}
									</button>
									<span className="text-sm text-slate-400">
										{selectedPost
											? "Changes update the selected post in place."
											: "Creating a post automatically selects it for comment work."}
									</span>
								</div>
							</form>
						</section>

						<section className={panelClass}>
							<div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
								<div>
									<p className="text-xs uppercase tracking-[0.24em] text-cyan-200/80">
										Library
									</p>
									<h2 className="mt-2 text-2xl font-semibold text-white">
										Posts
									</h2>
									<p className="mt-2 text-sm text-slate-400">
										Filter, open, edit, or delete any post in the system.
									</p>
								</div>
								<label className="block w-full max-w-md text-sm font-medium text-slate-200">
									Search
									<input
										className={inputClass}
										value={search}
										onChange={(event) => setSearch(event.target.value)}
										placeholder="Search titles and body text"
										disabled={!auth}
									/>
								</label>
							</div>

							{panelError ? (
								<div className="mt-5 rounded-2xl border border-amber-400/25 bg-amber-500/10 px-4 py-3 text-sm text-amber-100">
									{panelError}
								</div>
							) : null}

							<div className="mt-5 grid gap-4">
								{!auth ? (
									<div className="rounded-[1.75rem] border border-dashed border-white/10 bg-slate-950/40 px-6 py-10 text-center text-sm text-slate-400">
										Sign in to load the post list and continue into the API.
									</div>
								) : postsQuery.isLoading ? (
									<div className="rounded-[1.75rem] border border-white/10 bg-slate-950/40 px-6 py-10 text-sm text-slate-400">
										Loading posts…
									</div>
								) : visiblePosts.length === 0 ? (
									<div className="rounded-[1.75rem] border border-dashed border-white/10 bg-slate-950/40 px-6 py-10 text-center text-sm text-slate-400">
										No posts matched your search.
									</div>
								) : (
									visiblePosts.map((post) => {
										const isActive = post.id === selectedPostId;

										return (
											<article
												key={post.id}
												className={`cursor-pointer rounded-[1.75rem] border p-5 transition ${isActive ? "border-cyan-400/30 bg-cyan-400/10" : "border-white/10 bg-slate-950/40 hover:border-white/20 hover:bg-white/7"}`}
												onClick={() => {
													setSelectedPostId(post.id);
													setEditingCommentId(null);
													setCommentForm("");
												}}
											>
												<div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
													<div className="space-y-3">
														<div className="flex flex-wrap items-center gap-2">
															<span className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.2em] text-slate-300">
																Post #{post.id.slice(0, 8)}
															</span>
															{isActive ? (
																<span className="rounded-full border border-cyan-400/30 bg-cyan-400/10 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.2em] text-cyan-200">
																	Selected
																</span>
															) : null}
														</div>
														<h3 className="text-xl font-semibold text-white">
															{post.title}
														</h3>
														<p className="max-w-3xl text-sm leading-6 text-slate-300">
															{excerpt(post.content)}
														</p>
													</div>

													<div className="space-y-3 text-sm text-slate-400 md:text-right">
														<div>{formatDate(post.createdAt)}</div>
														<div>Author: {post.authorUsername ?? post.userId}</div>
														<div className="flex flex-wrap gap-2 md:justify-end">
															<button
																type="button"
																className={secondaryButtonClass}
																onClick={(event) => {
																	event.stopPropagation();
																	setSelectedPostId(post.id);
																	setEditingCommentId(null);
																	setCommentForm("");
																}}
															>
																Open
															</button>
															<button
																type="button"
																className={dangerButtonClass}
																disabled={deletePostMutation.isPending}
																onClick={(event) => {
																	event.stopPropagation();
																	deletePostMutation.mutate(post.id);
																}}
															>
																Delete
															</button>
														</div>
													</div>
												</div>
											</article>
										);
									})
								)}
							</div>
						</section>
					</section>

					<aside className="space-y-6">
						<section className={panelClass}>
							<div className="flex items-center justify-between gap-3">
								<div>
									<p className="text-xs uppercase tracking-[0.24em] text-cyan-200/80">
										Inspector
									</p>
									<h2 className="mt-2 text-xl font-semibold text-white">
										Selected post
									</h2>
								</div>
								{selectedPost ? (
									<span className="rounded-full border border-cyan-400/30 bg-cyan-400/10 px-3 py-1 text-xs font-semibold text-cyan-200">
										Active
									</span>
								) : null}
							</div>

							{selectedPost ? (
								<div className="mt-5 space-y-4">
									<div className={insetPanelClass}>
										<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
											Title
										</div>
										<div className="mt-2 text-lg font-semibold text-white">
											{selectedPost.title}
										</div>
									</div>
									<div className={insetPanelClass}>
										<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
											Content
										</div>
										<p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate-200">
											{selectedPost.content}
										</p>
									</div>
									<div className="grid gap-3 sm:grid-cols-2">
										<div className={insetPanelClass}>
											<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
												Created
											</div>
											<div className="mt-2 text-sm text-white">
												{formatDate(selectedPost.createdAt)}
											</div>
										</div>
										<div className={insetPanelClass}>
											<div className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
												Updated
											</div>
											<div className="mt-2 text-sm text-white">
												{formatDate(selectedPost.updatedAt)}
											</div>
										</div>
									</div>
								</div>
							) : (
								<div className="mt-5 rounded-[1.75rem] border border-dashed border-white/10 bg-slate-950/40 px-5 py-8 text-sm leading-6 text-slate-400">
									Select a post from the list to inspect details and comments.
								</div>
							)}
						</section>

						<section className={panelClass}>
							<div className="flex items-center justify-between gap-3">
								<div>
									<p className="text-xs uppercase tracking-[0.24em] text-cyan-200/80">
										Thread
									</p>
									<h2 className="mt-2 text-xl font-semibold text-white">
										Comments
									</h2>
								</div>
								{selectedPost ? (
									<span className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-semibold text-slate-300">
										{comments.length}
									</span>
								) : null}
							</div>

							{selectedPost ? (
								<>
									<form className="mt-5 space-y-4" onSubmit={handleSaveComment}>
										<label className="block text-sm font-medium text-slate-200">
											{editingCommentId ? "Edit comment" : "Add comment"}
											<textarea
												className={`${inputClass} min-h-30 resize-y`}
												value={commentForm}
												onChange={(event) => setCommentForm(event.target.value)}
												placeholder="Leave a concise note, question, or revision request"
												disabled={!auth}
											/>
										</label>

										<div className="flex flex-wrap items-center gap-3">
											<button
												type="submit"
												className={primaryButtonClass}
												disabled={!auth || upsertCommentMutation.isPending}
											>
												{upsertCommentMutation.isPending
													? editingCommentId
														? "Saving…"
														: "Posting…"
													: editingCommentId
														? "Update comment"
														: "Post comment"}
											</button>
											{editingCommentId ? (
												<button
													type="button"
													className={secondaryButtonClass}
													onClick={() => {
														setEditingCommentId(null);
														setCommentForm("");
													}}
												>
													Cancel edit
												</button>
											) : null}
										</div>
									</form>

									<div className="mt-5 space-y-3">
										{commentsQuery.isLoading ? (
											<div className="rounded-[1.75rem] border border-white/10 bg-slate-950/40 px-5 py-6 text-sm text-slate-400">
												Loading comments…
											</div>
										) : comments.length === 0 ? (
											<div className="rounded-[1.75rem] border border-dashed border-white/10 bg-slate-950/40 px-5 py-6 text-sm text-slate-400">
												No comments yet.
											</div>
										) : (
											comments.map((comment) => {
												const isEditing = comment.id === editingCommentId;

												return (
													<article
														key={comment.id}
														className={`rounded-3xl border p-4 ${isEditing ? "border-cyan-400/30 bg-cyan-400/10" : "border-white/10 bg-slate-950/40"}`}
													>
														<div className="flex items-start justify-between gap-3">
															<div className="space-y-2">
																<div className="text-sm font-semibold text-white">
																	{comment.authorUsername || comment.authorId}
																</div>
																<p className="whitespace-pre-wrap text-sm leading-6 text-slate-300">
																	{comment.content}
																</p>
																<div className="text-xs text-slate-500">
																	{formatDate(
																		comment.updatedAt ?? comment.createdAt,
																	)}
																</div>
															</div>
															<div className="flex shrink-0 gap-2">
																<button
																	type="button"
																	className={secondaryButtonClass}
																	onClick={() => {
																		setEditingCommentId(comment.id);
																		setCommentForm(comment.content);
																	}}
																>
																	Edit
																</button>
																<button
																	type="button"
																	className={dangerButtonClass}
																	disabled={deleteCommentMutation.isPending}
																	onClick={() => {
																		if (!activePostId) {
																			return;
																		}

																		deleteCommentMutation.mutate({
																			postId: activePostId,
																			commentId: comment.id,
																		});
																	}}
																>
																	Delete
																</button>
															</div>
														</div>
													</article>
												);
											})
										)}
									</div>
								</>
							) : (
								<div className="mt-5 rounded-[1.75rem] border border-dashed border-white/10 bg-slate-950/40 px-5 py-8 text-sm leading-6 text-slate-400">
									Pick a post to load its comments and start writing replies.
								</div>
							)}
						</section>
					</aside>
				</div>
			</main>
		</div>
	);
}

export default App;
