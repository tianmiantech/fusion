
export const getTokenName = ():string => {
  const { host } = window.location
  const tokenName = `fusion-x-user-token-${host}`;
  return tokenName;
};
