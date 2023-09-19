
export const getTokenName = ():string => {
  const tokenName = `fusion-${localStorage.getItem('env') || process.env.HOST_ENV}-x-user-token`;
  return tokenName;
};
