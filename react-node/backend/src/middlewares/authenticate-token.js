const jwt = require("jsonwebtoken");
const { ApiError } = require("../utils");
const { env } = require("../config");

const authenticateToken = (req, res, next) => {
  // Support Bearer token from Authorization header
  let accessToken = req.cookies.accessToken;
  let refreshToken = req.cookies.refreshToken;
  
  // Check for Bearer token in Authorization header
  const authHeader = req.headers.authorization;
  if (authHeader && authHeader.startsWith("Bearer ")) {
    accessToken = authHeader.substring(7); // Remove "Bearer " prefix
    // For Bearer token auth, we only need access token (refresh token optional)
    if (!accessToken) {
      throw new ApiError(401, "Unauthorized. Please provide valid access token.");
    }
    
    jwt.verify(accessToken, env.JWT_ACCESS_TOKEN_SECRET, (err, user) => {
      if (err) {
        throw new ApiError(
          401,
          "Unauthorized. Please provide valid access token."
        );
      }
      req.user = user;
      next();
    });
    return;
  }

  // Original cookie-based authentication
  if (!accessToken || !refreshToken) {
    throw new ApiError(401, "Unauthorized. Please provide valid tokens.");
  }

  jwt.verify(accessToken, env.JWT_ACCESS_TOKEN_SECRET, (err, user) => {
    if (err) {
      throw new ApiError(
        401,
        "Unauthorized. Please provide valid access token."
      );
    }

    jwt.verify(
      refreshToken,
      env.JWT_REFRESH_TOKEN_SECRET,
      (err, refreshToken) => {
        if (err) {
          throw new ApiError(
            401,
            "Unauthorized. Please provide valid refresh token."
          );
        }

        req.user = user;
        req.refreshToken = refreshToken;
        next();
      }
    );
  });
};

module.exports = { authenticateToken };
