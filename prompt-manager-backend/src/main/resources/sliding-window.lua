-- One atomic decision for a batch of model calls; use Redis time across app instances.
local stamp = redis.call('TIME')
local now = tonumber(stamp[1]) * 1000 + math.floor(tonumber(stamp[2]) / 1000)
local window = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])
local cost = tonumber(ARGV[3])
redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', now - window)
if redis.call('ZCARD', KEYS[1]) + cost > limit then return 0 end
for i = 1, cost do
  redis.call('ZADD', KEYS[1], now, ARGV[4] .. ':' .. i)
end
redis.call('PEXPIRE', KEYS[1], window)
return 1
