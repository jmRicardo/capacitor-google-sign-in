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


### Interfaces


#### SignInWithGoogleResponse

| Prop           | Type                                                                                                                                                                           |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`response`** | <code>{ user: string \| null; email: string \| null; givenName: string \| null; familyName: string \| null; identityToken: string; authorizationCode: string \| null; }</code> |

</docgen-api>
