# capacitor-google-sign-in

Plugin para SignUp/SignIn de Google en Android y iOS

## Install

```bash
npm install capacitor-google-sign-in
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`handleSignInButton()`](#handlesigninbutton)
* [`signOut()`](#signout)
* [`restorePreviousSignIn()`](#restoreprevioussignin)
* [`checkScope(...)`](#checkscope)
* [`requestScope(...)`](#requestscope)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### handleSignInButton()

```typescript
handleSignInButton() => Promise<SignInWithGoogleResponse>
```

**Returns:** <code>Promise&lt;<a href="#signinwithgoogleresponse">SignInWithGoogleResponse</a>&gt;</code>

--------------------


### signOut()

```typescript
signOut() => Promise<void>
```

--------------------


### restorePreviousSignIn()

```typescript
restorePreviousSignIn() => Promise<void>
```

--------------------


### checkScope(...)

```typescript
checkScope(options: { scope: string; }) => Promise<{ value: boolean; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ scope: string; }</code> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### requestScope(...)

```typescript
requestScope(options: { scope: string; }) => Promise<{ value: boolean; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ scope: string; }</code> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### Interfaces


#### SignInWithGoogleResponse

| Prop           | Type                                                                                                                                                                                                           |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`response`** | <code>{ user: string \| null; email: string \| null; givenName: string \| null; familyName: string \| null; identityToken: string; authorizationCode: string \| null; serverAuthCode: string \| null; }</code> |

</docgen-api>
